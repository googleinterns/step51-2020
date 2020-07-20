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
        double avgPageFactor = sumOfPageFactors/numPagesCrawled;
        return (Math.log(numPagesCrawled) + 1) * avgPageFactor;
    }

    // Crawls all pages from the domain to a depth of 1 to create the recommended list of pages.
    public static HashSet<String> getRecommendedLinks(String domain) {
        // use a hash set to avoid duplicate entries
        HashSet<String> recommendedLinks = new HashSet<String>();

        // begin with the domain
        recommendedLinks.add(domain);

        // add all the other links on the domain page
        try {
            // get the page HTML
            Document document = Jsoup.connect(domain).get();

            // parse the HTML to get the links to other URLs and add them to the hash set
            Elements pageLinks = document.select("a[href]");
            for (Element page : pageLinks) {
                recommendedLinks.add(page.attr("abs:href"));
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return recommendedLinks;
    }

    public static double getPageFactor(String url) {
        HashSet<String> keywords = new HashSet<String>();

        // add the significant keywords of the url to the hash set
        // split the url by [/ - . (blank space) _ :]
        String[] urlElements = url.split("/|\\-|\\.|\\s|\\_|\\:");
        for (String urlElement : urlElements) {
            // add all keywords resembling the element to the hash set
            ArrayList<String> relatedKeywords = getRelatedKeywords(urlElement);
            for (String keyword : relatedKeywords) {
                keywords.add(keyword);
            }
        }

        try {
            // get the page HTML
            Document document = Jsoup.connect(url).get();

            Elements pageLinks = document.select("title");

            // TODO
            
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    // Ensures that the keyword is significant, converts to lower case, and returns both the sigular and plural cases. 
    public static ArrayList<String> getRelatedKeywords(String word) {
        ArrayList<String> relatedKeywords = new ArrayList<String>();
        word = word.trim().toLowerCase();

        if (isSignificant(word)) {
            relatedKeywords.add(word);

            // add both the singular and plural forms
            if (word.charAt(word.length()-1) == 's') {
                relatedKeywords.add(word.substring(0, word.length()-1));
            } else {
                relatedKeywords.add(word += 's');
            }
        }
        
        return relatedKeywords;
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