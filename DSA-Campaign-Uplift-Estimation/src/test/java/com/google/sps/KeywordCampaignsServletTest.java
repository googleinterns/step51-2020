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
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        KeywordCampaign keywordCampaignObject = new KeywordCampaign("1", "2", "4 2 5", "entity 1", 432, 123, 42.51);
        ds.put(KeywordCampaignsServlet.createEntityFromKeywordCampaign(keywordCampaignObject));

        KeywordCampaignsServlet servlet = new KeywordCampaignsServlet();
        servlet.doGet(request, response);
        String result = sw.getBuffer().toString().trim();
        String expectedStr = "[{\"keywordCampaignId\":\"1\",\"userId\":\"2\",\"DSACampaignIds\":\"4 2 5\",\"name\":\"entity 1\",";
        expectedStr += "\"impressions\":432,\"clicks\":123,\"cost\":42.51}]";
        assertEquals(new String(expectedStr), result);
    }
 
    @Test
    public void keywordCampaignsServletDoPost() throws IOException, ServletException {
        when(request.getParameter("keywordCampaignId")).thenReturn("1");
        when(request.getParameter("userId")).thenReturn("2");
        when(request.getParameter("DSACampaignIds")).thenReturn("2 5 3 7");
        when(request.getParameter("name")).thenReturn("Test Keyword Campaign");
        when(request.getParameter("impressions")).thenReturn("12412");
        when(request.getParameter("clicks")).thenReturn("535");
        when(request.getParameter("cost")).thenReturn("2145.50");

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        assertEquals(0, ds.prepare(new Query("keywordCampaign")).countEntities(withLimit(10)));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        KeywordCampaignsServlet servlet = new KeywordCampaignsServlet();
        servlet.doPost(request, response);

        Query query = new Query("keywordCampaign");
    	Entity entity = ds.prepare(query).asSingleEntity();
        assertEquals("1", (String) entity.getProperty("keywordCampaignId"));
        assertEquals("2", (String) entity.getProperty("userId"));
        assertEquals("2 5 3 7", (String) entity.getProperty("DSACampaignIds"));
        assertEquals("Test Keyword Campaign", (String) entity.getProperty("name"));
        assertEquals(12412, (int) ((long) entity.getProperty("impressions")));
        assertEquals(535, (int) ((long) entity.getProperty("clicks")));
        assertEquals(2145.5, (double) entity.getProperty("cost"), .01);
    }
}