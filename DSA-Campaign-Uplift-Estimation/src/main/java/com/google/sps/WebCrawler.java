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

// Implements all functions that require the use of jsoup.
public class WebCrawler {

    public static double getWebsiteFactor(Entity keywordCampaignEntity, Entity DSACampaignEntity) {
        HashSet<String> recommendedLinks = getRecommendedLinks((String) DSACampaignEntity.getProperty("domain"));

        // add the target pages to the recommended links
        String[] targetPages = ((String) DSACampaignEntity.getProperty("targets")).split(",");
        for (String targetPage : targetPages) {
            targetPage = targetPage.trim();

            // avoid duplicate entries
            if (!recommendedLinks.contains(targetPage)) {
                recommendedLinks.add(targetPage);
            }
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
        // use a hash set to easily deal with duplicate entries
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
                String url = page.attr("abs:href");
                
                // avoid duplicate entries
                if (!recommendedLinks.contains(url)) {
                    recommendedLinks.add(url);
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return recommendedLinks;
    }

    public static double getPageFactor(String url) {
        // TODO
        return 1;
    }
}