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
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.SortDirection;
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
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.util.ArrayList;
import com.google.gson.Gson;

/*
 * Tests the doGet() and doPost() functions in KeywordCampaignsServlet.java.
 */
@RunWith(JUnit4.class)
public final class KeywordCampaignsServletTest {
    
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
    public void keywordCampaignsServletDoGet() throws IOException, ServletException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        KeywordCampaign keywordCampaignObject1 = new KeywordCampaign("1", "2", "entity 1", 123, "United States of America", "California, Texas", 432, 123, 42.51);
        ds.put(KeywordCampaignsServlet.createEntityFromKeywordCampaign(keywordCampaignObject1));

        KeywordCampaign keywordCampaignObject2 = new KeywordCampaign("1", "2", "entity 2", 123, "United States of America", "California, Texas", 432, 123, 42.51);
        ds.put(KeywordCampaignsServlet.createEntityFromKeywordCampaign(keywordCampaignObject2));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        KeywordCampaignsServlet servlet = new KeywordCampaignsServlet();
        servlet.doGet(request, response);
        String result = sw.getBuffer().toString().trim();

        ArrayList<KeywordCampaign> expectedKeywordCampaigns = new ArrayList<KeywordCampaign>();
        expectedKeywordCampaigns.add(keywordCampaignObject1);
        expectedKeywordCampaigns.add(keywordCampaignObject2);

        Gson gson = new Gson();
        String expectedJson = gson.toJson(expectedKeywordCampaigns);

        assertEquals(expectedJson, result);
    }
 
    @Test
    public void keywordCampaignsServletDoPost() throws IOException, ServletException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        assertEquals(0, ds.prepare(new Query("keywordCampaign")).countEntities(withLimit(10)));

        KeywordCampaign keywordCampaignObject = new KeywordCampaign("1", "2", "Test Keyword Campaign", 123, "United States of America", "California, Texas", 12412, 535, 2145.50);
        ds.put(KeywordCampaignsServlet.createEntityFromKeywordCampaign(keywordCampaignObject));

        assertEquals(1, ds.prepare(new Query("keywordCampaign")).countEntities(withLimit(10)));

        Query query = new Query("keywordCampaign");
    	Entity entity = ds.prepare(query).asSingleEntity();
        assertEquals("1", (String) entity.getProperty("keywordCampaignId"));
        assertEquals("2", (String) entity.getProperty("userId"));
        assertEquals("Test Keyword Campaign", (String) entity.getProperty("name"));
        assertEquals(123, (double) entity.getProperty("manualCPC"), .01);
        assertEquals("United States of America", (String) entity.getProperty("locations"));
        assertEquals("California, Texas", (String) entity.getProperty("negativeLocations"));
        assertEquals(12412, (int) ((long) entity.getProperty("impressions")));
        assertEquals(535, (int) ((long) entity.getProperty("clicks")));
        assertEquals(2145.5, (double) entity.getProperty("cost"), .01);
    }

    @Test
    public void KeywordCampaignsServletGetCampaignId() throws IOException, ServletException {
        // test getting unique keyword campaign ids
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        assertEquals(0, ds.prepare(new Query("numKeywordCampaigns")).countEntities(withLimit(10)));

        String id = KeywordCampaignsServlet.getNewCampaignId(true);
        assertEquals(1, ds.prepare(new Query("numKeywordCampaigns")).countEntities(withLimit(10)));
        assertEquals("1", id);

        id = KeywordCampaignsServlet.getNewCampaignId(true);
        assertEquals(1, ds.prepare(new Query("numKeywordCampaigns")).countEntities(withLimit(10)));
        assertEquals("2", id);

        // test getting unique DSA campaign ids
        assertEquals(0, ds.prepare(new Query("numDSACampaigns")).countEntities(withLimit(10)));

        id = KeywordCampaignsServlet.getNewCampaignId(false);
        assertEquals(1, ds.prepare(new Query("numDSACampaigns")).countEntities(withLimit(10)));
        assertEquals("1", id);

        id = KeywordCampaignsServlet.getNewCampaignId(false);
        assertEquals(1, ds.prepare(new Query("numDSACampaigns")).countEntities(withLimit(10)));
        assertEquals("2", id);
    }
}