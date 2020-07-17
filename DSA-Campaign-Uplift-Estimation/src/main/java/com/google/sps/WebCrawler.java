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

// Crawls all pages from the domain to a depth of 1 to create the recommended list of pages.
public class WebCrawler {

    public static double getWebsiteFactor(Entity keywordCampaignEntity, Entity DSACampaignEntity) {
        // TODO
        return 1;
    }

    // Builds and returns the recommended list of pages for the given domain.
    public static HashSet<String> getRecommendedLinks(String domain) {
        // use a hash set to easily deal with duplicate entries
        HashSet<String> recommendedLinks= new HashSet<String>();

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
                
                // make sure we haven't already added the url
                if (!recommendedLinks.contains(url)) {
                    recommendedLinks.add(url);
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return recommendedLinks;
    }
}