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
 * Tests the doPost() function in DSACampaignDataServlet.java.
 */
@RunWith(JUnit4.class)
public final class DSACampaignDataServletTest {
    
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
    public void DSACampaignDataServletdoPost() throws IOException, ServletException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        KeywordCampaign keywordCampaignObject = new KeywordCampaign("1", "1", "Test KC", .4, "United States", "Texas, California", 1500, 300, 75.12);
        ds.put(KeywordCampaignsServlet.createEntityFromKeywordCampaign(keywordCampaignObject));

        assertEquals(1, ds.prepare(new Query("keywordCampaign")).countEntities(withLimit(10)));

        DSACampaign DSACampaignObjectPending = new DSACampaign("1", "1", "1", "Test DC Pending", "pending", "1/1/1", "2/2/2", .8, 500, "United States",
            "California, Texas", "https://www.google.com/", "https://www.google.com/", "sample ad text 2", 0, 0, 0);
        ds.put(DSACampaignsServlet.createEntityFromDSACampaign(DSACampaignObjectPending));

        DSACampaign DSACampaignObjectComplete = new DSACampaign("2", "1", "1", "Test DC Complete", "complete", "1/1/1", "2/2/2", .8, 500, "United States",
            "California, Texas", "https://www.google.com/", "https://www.google.com/", "sample ad text 2", 0, 0, 0);
        ds.put(DSACampaignsServlet.createEntityFromDSACampaign(DSACampaignObjectComplete));

        assertEquals(2, ds.prepare(new Query("DSACampaign")).countEntities(withLimit(10)));

        DSACampaignDataServlet servlet = new DSACampaignDataServlet();
        servlet.doPost(request, response);

        Query firstQuery = new Query("DSACampaign").setFilter(new Query.FilterPredicate("DSACampaignId", Query.FilterOperator.EQUAL, "1"));
        Entity pendingDSACampaignEntity = ds.prepare(firstQuery).asSingleEntity();

        // The pending DSA campaign entity should be changed to complete and the estimation results should be updated.
        assertEquals("complete", (String) pendingDSACampaignEntity.getProperty("campaignStatus"));
        assertEquals(2671, (int) ((long) pendingDSACampaignEntity.getProperty("impressions")));
        assertEquals(480, (int) ((long) pendingDSACampaignEntity.getProperty("clicks")));
        assertEquals(384, (double) pendingDSACampaignEntity.getProperty("cost"), .01);

        Query secondQuery = new Query("DSACampaign").setFilter(new Query.FilterPredicate("DSACampaignId", Query.FilterOperator.EQUAL, "2"));
        Entity completeDSACampaignEntity = ds.prepare(secondQuery).asSingleEntity();

        // The complete DSA campaign entity should remain unchanged.
        assertEquals("complete", (String) completeDSACampaignEntity.getProperty("campaignStatus"));
        assertEquals(0, (int) ((long) completeDSACampaignEntity.getProperty("impressions")));
        assertEquals(0, (int) ((long) completeDSACampaignEntity.getProperty("clicks")));
        assertEquals(0, (double) completeDSACampaignEntity.getProperty("cost"), .01);
    }
}