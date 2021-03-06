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
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;
import java.io.IOException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.HashSet;

// Tests the getRecommendedLinks() function in WebCrawler.java.
@RunWith(JUnit4.class)
public final class WebCrawlerTest {

    @Test
    public void WebCrawlerResembles() throws IOException {
        // true cases

        assertTrue(WebCrawler.resembles("abcdefg", "abcdefg"));

        // cases testing insertion, deletion, and swap
        assertTrue(WebCrawler.resembles("abcdefg", "abcefg"));
        assertTrue(WebCrawler.resembles("abcdefg", "abchefg"));
        assertTrue(WebCrawler.resembles("abcdefg", "abchdefg"));        

        // ending cases
        assertTrue(WebCrawler.resembles("abcdefg", "abcdef"));
        assertTrue(WebCrawler.resembles("abcdefg", "abcdefgh"));
        assertTrue(WebCrawler.resembles("abcdefg", "abcdefh"));

        // starting cases
        assertTrue(WebCrawler.resembles("abcdefg", "bcdefg"));
        assertTrue(WebCrawler.resembles("abcdefg", "hbcdefg"));
        assertTrue(WebCrawler.resembles("abcdefg", "habcdefg"));

        // edge cases
        assertTrue(WebCrawler.resembles("", ""));
        assertTrue(WebCrawler.resembles("", "a"));
        assertTrue(WebCrawler.resembles("a", ""));
        assertTrue(WebCrawler.resembles("a", "b"));

        // false cases

        assertFalse(WebCrawler.resembles("abcdefg", "hfhjkja"));

        // invalid lengths
        assertFalse(WebCrawler.resembles("abcdefg", "abcdefghi"));
        assertFalse(WebCrawler.resembles("abcdefghi", "abcdefg"));

        // >1 differences
        assertFalse(WebCrawler.resembles("abcdefg", "ahcdegg"));
        assertFalse(WebCrawler.resembles("acdefg", "abciefg"));
        assertFalse(WebCrawler.resembles("abcefg", "abcdef"));
        assertFalse(WebCrawler.resembles("bcdefg", "abcdef"));

        // other tests
        assertTrue(WebCrawler.resembles("abcd", "acbcd"));
        assertTrue(WebCrawler.resembles("aaaaa", "aaaa"));
        assertTrue(WebCrawler.resembles("aaaa", "aaaaa"));
        assertTrue(WebCrawler.resembles("aaaaa", "aacaa"));
        assertTrue(WebCrawler.resembles("aaaaa", "aacaaa"));
        assertFalse(WebCrawler.resembles("aaaaa", "aaca"));

        // real world cases

        // plural vs single
        assertTrue(WebCrawler.resembles("flower", "flowers"));
        // typo
        assertTrue(WebCrawler.resembles("flower", "flowur"));
    }
}