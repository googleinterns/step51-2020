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
import java.io.IOException;
import java.util.HashSet;
import java.util.ArrayList;

// Implements all of the functions that require the use of jsoup.
public class WebCrawler {

    public static double getWebsiteFactor(Entity keywordCampaignEntity, Entity DSACampaignEntity) {
        try {
            // use a hashset to avoid duplicate entries
            HashSet<String> recommendedLinks = getRecommendedLinks((String) DSACampaignEntity.getProperty("domain"));

            // add the target pages to the recommended links
            String[] targetPages = ((String) DSACampaignEntity.getProperty("targets")).split(",");
            for (String targetPage : targetPages) {
                recommendedLinks.add(targetPage.trim());
            }

            double sumOfPageFactors = 0;
            for (String url : recommendedLinks) {
                sumOfPageFactors += getPageFactor(url);
            }   

            // final website factor calculations
            int numPagesCrawled = recommendedLinks.size();
            double avgPageFactor = sumOfPageFactors / numPagesCrawled;
            double logBase100 = Math.log10(numPagesCrawled) / 2;
            return (logBase100 + 1) * avgPageFactor;
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        // failed to obtain the website factor
        return 1;
    }

    // Crawls all pages from the domain to a depth of 1 to create the recommended list of pages.
    public static HashSet<String> getRecommendedLinks(String domain) throws IOException {
        // use a hash set to avoid duplicate entries
        HashSet<String> recommendedLinks = new HashSet<String>();

        // begin with the domain
        recommendedLinks.add(domain);

        // get the page HTML
        Document document = Jsoup.connect(domain).get();

        // parse the HTML to get the links to other URLs and add them to the hash set
        Elements pageLinks = document.select("a[href]");
        for (Element page : pageLinks) {
            recommendedLinks.add(page.attr("abs:href"));
        }

        return recommendedLinks;
    }

    public static double getPageFactor(String url) throws IOException {
        // get the page HTML
        Document document = Jsoup.connect(url).get();

        // get the significant keywords from the url and title
        HashSet<String> keywordsURLTitle = getKeywordsFromURLAndTitle(url, document);
        
        // get the significant keywords from the meta description and headers
        HashSet<String> keywordsDescriptionHeaders = getKeywordsFromDescriptionAndHeaders(document);

        /*
         * Go through every element of keywordsDescriptionHeaders and check if the word or a close variation of it (difference of one character) is found in keywordsURLTitle.
         * If so, add the word to sharedKeywords.
         */
        HashSet<String> sharedKeywords = new HashSet<String>();
        for (String keyword : keywordsDescriptionHeaders) {
            for (String matchingKeyword : keywordsURLTitle) {
                if (!sharedKeywords.contains(matchingKeyword) && resembles(keyword, matchingKeyword)) {
                    sharedKeywords.add(matchingKeyword);
                    break;
                }
            }
        }

        return (((double) sharedKeywords.size()) / ((double) keywordsURLTitle.size())) + 1;
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
         * foundDifference will be false if, when going left to right, we haven't found a difference between the two strings yet, and true if we have found a difference.
         */
        boolean foundDifference = false;
        int posStr1 = 0;
        int posStr2 = 0;
        while ((posStr1 < str1.length()) && (posStr2 < str2.length())) {
            if (str1.charAt(posStr1) == str2.charAt(posStr2)) {
                // found no difference, keep progressing through the strings
                posStr1++;
                posStr2++;
            } else if (!foundDifference) {
                // found a difference between the two strings
                foundDifference = true;
                
                if ((posStr1 == str1.length()-1) || (posStr2 == str2.length()-1)) {
                    // Case 1: We have finished traversing through either str1 or str2. This case is handled outside of the while loop, which is why we simply increment
                    // posStr1 and posStr2 to break out of the while loop on the next iteration.
                    posStr1++;
                    posStr2++;
                } else if (str1.charAt(posStr1+1) == str2.charAt(posStr2)) {
                    // Case 2: Ex: str1 = abc'd'efg and str2 = abc'e'fg. The two strings get back in line if we only increment posStr1 (what's left in both strings is "efg").
                    posStr1++;
                } else if (str1.charAt(posStr1+1) == str2.charAt(posStr2+1)) {
                    // Case 3: Ex: str1 = abc'd'efg and str2 = abc'h'efg. The two strings get back in line if we increment both posStr1 and posStr2 (what's left in both strings is "efg").
                    posStr1++;
                    posStr2++;
                } else if (str1.charAt(posStr1) == str2.charAt(posStr2+1)) {
                    // Case 4: Ex: str1 = abc'd'efg and str2 = abc'h'defg. The two strings get back in line if we only increment posStr2 (what's left in both strings is "defg").
                    posStr2++;
                }
            } else {
                // found 2 differences in the strings
                return false;
            }
        }

        // Case where we have traversed through at least one of the strings, while having found 1 difference. Then if we haven't traversed completely through both strings,
        // then there is additional character left in one of the strings, meaning there's a second difference.
        if (foundDifference && !((posStr1 == str1.length()) && (posStr2 == str2.length()))) {
            return false;
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
        if (word.length() == 0) {
            return false;
        }
        // common url elements
        if (word.equals("https") || word.equals("www") || word.equals("com") || word.equals("org") || word.equals("html") || word.equals("php")) {
            return false;
        }
        // articles
        if (word.equals("the") || word.equals("a") || word.equals("an")) {
            return false;
        }
        return true;
    }
}