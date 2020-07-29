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

package com.google.sps.servlets;

import com.google.sps.classes.KeywordCampaign;
import com.google.sps.classes.DSACampaign;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Query;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

/*
 * Tests the doPost() function in CampaignEstimationResultsServlet.java.
 */
@RunWith(JUnit4.class)
public final class CampaignEstimationResultsServletTest {
    
    @Mock
    HttpServletRequest request;
 
    @Mock
    HttpServletResponse response;
    
    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        helper.setUp();
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }

    @Test
    public void DSACampaignDataServletGetLocationsFactor() throws IOException, ServletException {
        KeywordCampaign KeywordCampaignObject = new KeywordCampaign("1", "1", "Test KC", .8, "TX, CA, MI",
            "", 0, 0, 0);
        Entity keywordCampaignEntity = KeywordCampaignsServlet.createEntityFromKeywordCampaign(KeywordCampaignObject);

        DSACampaign DSACampaignObject= new DSACampaign("2", "1", "1", "Test DC", "pending", "1/1/1", "2/2/2", .8, 500, "USA",
            "CA, TX", "http://dsa-uplift-estimation-2020.uc.r.appspot.com/Home/home.html", "", "sample ad text 2", 0, 0, 0, null);
        Entity DSACampaignEntity = DSACampaignsServlet.createEntityFromDSACampaign(DSACampaignObject);

        assertEquals(1.81904, CampaignEstimationResultsServlet.getLocationsFactor(keywordCampaignEntity, DSACampaignEntity), .01);
    }
}