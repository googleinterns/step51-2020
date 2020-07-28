// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EmbeddedEntity;
import java.io.IOException;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;

// Implements all of the functions that require the use of jsoup.
public class WebCrawler {

    public Entity keywordCampaignEntity;
    public Entity DSACampaignEntity;

    // key = query, value = associated url
    public HashMap<String, String> SQR;
    
    public WebCrawler(Entity keywordCampaignEntity, Entity DSACampaignEntity) {
        this.keywordCampaignEntity = keywordCampaignEntity;
        this.DSACampaignEntity = DSACampaignEntity;
        SQR = new HashMap<String, String>();
    }

    public double getWebsiteFactor() {
        try {
            // use a hashset to avoid duplicate entries
            HashSet<String> recommendedLinks = getRecommendedLinks((String) DSACampaignEntity.getProperty("domain"));

            // add the target pages to the recommended links
            String[] targetPages = ((String) DSACampaignEntity.getProperty("targets")).split(",");
            for (String targetPage : targetPages) {
                String refinedURL = targetPage.trim();
                if (!refinedURL.equals("")) {
                    recommendedLinks.add(refinedURL);
                }
            }

            double sumOfPageFactors = 0;
            for (String url : recommendedLinks) {
                sumOfPageFactors += getPageFactor(url);
            }   

            // final website factor calculations
            int numPagesCrawled = recommendedLinks.size();
            double avgPageFactor = sumOfPageFactors / numPagesCrawled;
            double logBase100 = Math.log10(numPagesCrawled) / 2;
            return Math.max((logBase100 + 1) * avgPageFactor, 1.3);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        // failed to obtain the website factor
        return 1;
    }

    /*
     * Converts the SQR data from a hash map to an embedded entity (entity will be embedded in the associated DSA campaign entity).
     * The SQR should have first been populated by calling getWebsiteFactor(); otherwise, the SQR will be empty.
     */
    public EmbeddedEntity getSQR() {
        EmbeddedEntity SQREmbeddedEntity = new EmbeddedEntity();
        SQREmbeddedEntity.setProperty("numLines", SQR.size());

        int lineNum = 1;
        for (String query : SQR.keySet()) {
            String propertyName = "Line " + lineNum;
            SQREmbeddedEntity.setProperty(propertyName + " Query", query);
            SQREmbeddedEntity.setProperty(propertyName + " URL", SQR.get(query));
            lineNum++;
        }

        return SQREmbeddedEntity;
    }

    // Crawls all pages from the domain to a depth of 1 to create the recommended list of pages.
    public static HashSet<String> getRecommendedLinks(String domain) throws IOException {
        // use a hash set to avoid duplicate entries
        HashSet<String> recommendedLinks = new HashSet<String>();

        // check that the domain isn't empty
        if (!domain.trim().equals("")) {
            // begin with the domain
            recommendedLinks.add(domain);

            // get the page HTML
            Document document = Jsoup.connect(domain).get();

            // parse the HTML to get the links to other URLs and add them to the hash set
            Elements pageLinks = document.select("a[href]");
            for (Element page : pageLinks) {
                String pageLink = page.attr("abs:href").trim();
                if (!pageLink.equals("")){
                    recommendedLinks.add(pageLink);
                }
            }
        }

        return recommendedLinks;
    }

    public double getPageFactor(String url) throws IOException {
        // get the page HTML
        Document document = Jsoup.connect(url).get();

        // get the significant keywords from the url and title
        HashSet<String> keywordsURLTitle = getKeywordsFromURLAndTitle(url, document);
        
        // get the significant keywords from the meta description and headers
        HashSet<String> keywordsDescriptionHeaders = getKeywordsFromDescriptionAndHeaders(document);

        /*
         * Go through every element of keywordsDescriptionHeaders and check if the word or a close variation of it (difference of one character) is found in keywordsURLTitle.
         * If so, add the word to the SQR (this word has the strongest chance of becoming a query).
         */
        for (String keyword : keywordsDescriptionHeaders) {
            for (String matchingKeyword : keywordsURLTitle) {
                if (!SQR.containsKey(matchingKeyword) && resembles(keyword, matchingKeyword)) {
                    SQR.put(matchingKeyword, url);
                    break;
                }
            }
        }

        return (((double) SQR.size()) / ((double) keywordsURLTitle.size())) + 1;
    }

    // Returns true if there is less than 1 character difference between strings str1 and str2.
    public static boolean resembles(String str1, String str2) {
        // check if they are the same to save time
        if (str1.equals(str2)) {
            return true;
        }

        // initial check to ensure that the lengths of the two strings are within 1 of each other
        if ((str1.length() < str2.length() - 1) || (str1.length() > str2.length() + 1)) {
            return false;
        }

        /*
         * We will go from left to right through str1 and str2, comparing the characters of the two strings.
         * posStr1 and posStr2 represent the character positions we are analyzing in str1 and str2, respectively.
         * If we find a difference between the 2 strings, we will analyze what's left of the two strings to compare.
         * - If the remaining substrings fall into one of the three cases explained in the code (swap, insertion, or deletion),
         * - we return true; otherwise, we return false.
         */
        int posStr1 = 0;
        int posStr2 = 0;
        while ((posStr1 < str1.length()) && (posStr2 < str2.length())) {
            if (str1.charAt(posStr1) == str2.charAt(posStr2)) {
                // found no difference, keep progressing through the strings
                posStr1++;
                posStr2++;
            } else {
                // obtain what's left to compare (e.g. ab'c'def => def)
                String remainingPortionStr1 = str1.substring(posStr1+1);
                String remainingPortionStr2 = str2.substring(posStr2+1);

                if (remainingPortionStr1.equals(remainingPortionStr2)) {
                    // Case 1 (swap): Ex: str1 = abc'd'efg and str2 = abc'h'efg
                    return true;
                } else if ((str1.charAt(posStr1) + remainingPortionStr1).equals(remainingPortionStr2)) {
                    // Case 2 (insertion): Ex: str1 = abc'd'efg and str2 = abc'h'defg
                    return true;
                } else if (remainingPortionStr1.equals(str2.charAt(posStr2) + remainingPortionStr2)) {
                    // Case 3 (deletion): Ex: str1 = abc'd'efg and str2 = abc'e'fg
                    return true;
                } else {
                    return false;
                }
            }
        }

        // successfully traversed through the strings having found <2 differences
        return true;
    }

    // Returns in a hashset all of the significant keywords from the url and title.
    public static HashSet<String> getKeywordsFromURLAndTitle(String url, Document document) {
        HashSet<String> keywords = new HashSet<String>();

        // add the significant keywords of the url to the hash set
        extractKeywords(url, keywords);

        // add the significant keywords of the title to the hash set
        getKeywordsFromPageElements(document, "title", keywords);

        return keywords;
    }

    // Returns in a hashset all of the significant keywords from the meta description and headers.
    public static HashSet<String> getKeywordsFromDescriptionAndHeaders(Document document) {
        HashSet<String> keywords = new HashSet<String>();

        // add the significant keywords of the description to the hash set
        Elements descriptions = document.select("meta[name=description]");
        for (Element description : descriptions) {
            extractKeywords(description.attr("content"), keywords);
        }

        // add the significant keywords of the h1, h2, and h3 headers to the hash set
        getKeywordsFromPageElements(document, "h1", keywords);
        getKeywordsFromPageElements(document, "h2", keywords);
        getKeywordsFromPageElements(document, "h3", keywords);

        return keywords;
    }

    // Extracts the keywords from all of the elements on the page with the specified tag.
    public static void getKeywordsFromPageElements(Document document, String tagName, HashSet<String> keywords) {
        Elements elements = document.select(tagName);
        for (Element element : elements) {
            extractKeywords(element.text(), keywords);
        }
    }

    // Splits the string element by common delimiters and extracts from the string significant keywords.
    public static void extractKeywords(String element, HashSet<String> keywords) {
        String[] elementArr = element.split("/|\\-|\\.|\\s|\\_|\\:|\\,|\\?|\\!|\\;");
        for (String word : elementArr) {
            word = word.trim().toLowerCase();

            if (isSignificant(word)) {
                keywords.add(word);
            }
        }
    }

    // Checks if the word has meaningful content.
    public static boolean isSignificant(String word) {
        // ignore empty and 1-letter strings
        if (word.length() <= 1) {
            return false;
        }
        // common url elements
        if (word.equals("https") || word.equals("http") || word.equals("www") || word.equals("com") || word.equals("org") || word.equals("html") || word.equals("php")) {
            return false;
        }
        // articles
        if (word.equals("the") || word.equals("a") || word.equals("an")) {
            return false;
        }
        return true;
    }
}