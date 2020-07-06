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
import java.util.ArrayList;

/*
 * Tests the doGet() and doPost() functions in the campaign java servlets.
 */
@RunWith(JUnit4.class)
public final class CampaignServletTest {
    
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
        Entity keywordCampaignEntity = new Entity("keywordCampaign");
        keywordCampaignEntity.setProperty("keywordCampaignId", 1);
        keywordCampaignEntity.setProperty("userId", 2);
        keywordCampaignEntity.setProperty("name", "entity 1");
        keywordCampaignEntity.setProperty("fromDate", "1/1/1");
        keywordCampaignEntity.setProperty("toDate", "2/2/2");
        keywordCampaignEntity.setProperty("dailyBudget", 123.2);
        keywordCampaignEntity.setProperty("location", "CA");
        keywordCampaignEntity.setProperty("domain", "google.com");
        keywordCampaignEntity.setProperty("target", "google.com");
        keywordCampaignEntity.setProperty("impressions", 432);
        keywordCampaignEntity.setProperty("clicks", 123);
        keywordCampaignEntity.setProperty("cost", 42.51);
        ArrayList<Integer> DSACampaignIds = new ArrayList<Integer>();
        DSACampaignIds.add(4);
        DSACampaignIds.add(2);
        DSACampaignIds.add(5);
        keywordCampaignEntity.setProperty("DSACampaignIds", DSACampaignIds);
        ds.put(keywordCampaignEntity);

        KeywordCampaignsServlet servlet = new KeywordCampaignsServlet();
        servlet.doGet(request, response);
        String result = sw.getBuffer().toString().trim();
        assertEquals(new String("[{\"keywordCampaignId\":1,\"userId\":2,\"DSACampaignIds\":[4,2,5],\"name\":\"entity 1\",\"fromDate\":\"1/1/1\",\"toDate\":\"2/2/2\",\"dailyBudget\":123.2,\"location\":\"CA\",\"domain\":\"google.com\",\"target\":\"google.com\",\"impressions\":432,\"clicks\":123,\"cost\":42.51}]"), result);
    }
 
    @Test
    public void keywordCampaignsServletDoPost() throws IOException, ServletException {
        when(request.getParameter("keywordCampaignId")).thenReturn("1");
        when(request.getParameter("userId")).thenReturn("2");
        when(request.getParameter("name")).thenReturn("Test Keyword Campaign");
        when(request.getParameter("fromDate")).thenReturn("1/1/1");
        when(request.getParameter("toDate")).thenReturn("2/2/2");
        when(request.getParameter("dailyBudget")).thenReturn("20.12");
        when(request.getParameter("location")).thenReturn("CA");
        when(request.getParameter("domain")).thenReturn("google.com");
        when(request.getParameter("target")).thenReturn("google.com");
        when(request.getParameter("impressions")).thenReturn("12412");
        when(request.getParameter("clicks")).thenReturn("535");
        when(request.getParameter("cost")).thenReturn("2145.50");
        when(request.getParameter("DSACampaignIds")).thenReturn("2 5 3 7");

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        assertEquals(0, ds.prepare(new Query("keywordCampaign")).countEntities(withLimit(10)));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        KeywordCampaignsServlet servlet = new KeywordCampaignsServlet();
        servlet.doPost(request, response);

        Query query = new Query("keywordCampaign");
    	Entity entity = ds.prepare(query).asSingleEntity();
        assertEquals(1, (int) ((long) entity.getProperty("keywordCampaignId")));
        assertEquals(2, (int) ((long) entity.getProperty("userId")));
        assertEquals("Test Keyword Campaign", (String) entity.getProperty("name"));
        assertEquals("1/1/1", (String) entity.getProperty("fromDate"));
        assertEquals("2/2/2", (String) entity.getProperty("toDate"));
        assertEquals(20.12, (double) entity.getProperty("dailyBudget"), .01);
        assertEquals("CA", (String) entity.getProperty("location"));
        assertEquals("google.com", (String) entity.getProperty("domain"));
        assertEquals("google.com", (String) entity.getProperty("target"));
        assertEquals(12412, (int) ((long) entity.getProperty("impressions")));
        assertEquals(535, (int) ((long) entity.getProperty("clicks")));
        assertEquals(2145.5, (double) entity.getProperty("cost"), .01);

        ArrayList<Integer> DSACampaignIds = (ArrayList<Integer>) entity.getProperty("DSACampaignIds");
        assertEquals(new Long(2), DSACampaignIds.get(0));
        assertEquals(new Long(5), DSACampaignIds.get(1));
        assertEquals(new Long(3), DSACampaignIds.get(2));
        assertEquals(new Long(7), DSACampaignIds.get(3));
        assertEquals(4, DSACampaignIds.size());
    }
}