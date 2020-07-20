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

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import java.io.IOException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.HashSet;

// Tests the getRecommendedLinks() function in WebCrawler.java.
@RunWith(JUnit4.class)
public final class WebCrawlerTest {

    @Test
    public void WebCrawlerGetRecommendedLinks() throws IOException {
        // crawl the home page of the deployed website
        HashSet<String> recommendedLinks = WebCrawler.getRecommendedLinks("http://dsa-uplift-estimation-2020.uc.r.appspot.com/Home/home.html");

        // these are links on the home page
        HashSet<String> expectedRecommendedLinks = new HashSet<String>();
        expectedRecommendedLinks.add("");
        expectedRecommendedLinks.add("http://dsa-uplift-estimation-2020.uc.r.appspot.com/Compare/compare.html");
        expectedRecommendedLinks.add("http://dsa-uplift-estimation-2020.uc.r.appspot.com/Home/home.html");
        expectedRecommendedLinks.add("http://dsa-uplift-estimation-2020.uc.r.appspot.com/Create/create.html");
        expectedRecommendedLinks.add("http://dsa-uplift-estimation-2020.uc.r.appspot.com/Home/home.html#");

        assertEquals(expectedRecommendedLinks, recommendedLinks);
    }

    @Test
    public void WebCrawlerGetKeywordsFromURLAndTitle() throws IOException {
        // obtain the significant keywords of the url and title of the GitHub repo
        HashSet<String> keywords = WebCrawler.getKeywordsFromURLAndTitle("https://github.com/googleinterns/step51-2020");

        // these are links on the home page
        HashSet<String> expectedKeywords = new HashSet<String>();
        expectedKeywords.add("github");
        expectedKeywords.add("googleinterns");
        expectedKeywords.add("step51");
        expectedKeywords.add("2020");
        expectedKeywords.add("google");
        expectedKeywords.add("step");
        expectedKeywords.add("capstone");
        expectedKeywords.add("project");

        assertEquals(expectedKeywords, keywords);
    }
}