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
 * Tests the doGet() and doPost() functions in DSACampaignsServlet.java.
 */
@RunWith(JUnit4.class)
public final class DSACampaignsServletTest {
    
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
    public void DSACampaignsServletDoGet() throws IOException, ServletException {
        when(request.getParameter("keywordCampaignId")).thenReturn("1");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        DSACampaign DSACampaignObject = new DSACampaign("1", "2", "1", "entity 1", "pending", "1/1/1", "2/2/2", 23.1, 123.2, "California Texas", "google.com",
            "test1.com test2.com", "sample ad text", 432, 123, 42.51);
        ds.put(DSACampaignsServlet.createEntityFromDSACampaign(DSACampaignObject));

        DSACampaignsServlet servlet = new DSACampaignsServlet();
        servlet.doGet(request, response);
        String result = sw.getBuffer().toString().trim();
        String expectedStr = "[{\"DSACampaignId\":1,\"userId\":2,\"keywordCampaignId\":1,\"name\":\"entity 1\",\"campaignStatus\":\"pending\",\"startDate\":\"1/1/1\",\"endDate\":\"2/2/2\",";
        expectedStr += "\"manualCPC\":23.1,\"dailyBudget\":123.2,\"locations\":\"California Texas\",\"domain\":\"google.com\",\"targets\":\"test1.com test2.com\",";
        expectedStr += "\"adText\":\"sample ad text\",\"impressions\":432,\"clicks\":123,\"cost\":42.51}]";
        assertEquals(new String(expectedStr), result);
    }

    @Test
    public void DSACampaignsServletDoPost() throws IOException, ServletException {
        when(request.getParameter("DSACampaignId")).thenReturn("3");
        when(request.getParameter("userId")).thenReturn("2");
        when(request.getParameter("keywordCampaignId")).thenReturn("1");
        when(request.getParameter("name")).thenReturn("Test DSA Campaign");
        when(request.getParameter("campaignStatus")).thenReturn("complete");
        when(request.getParameter("startDate")).thenReturn("1/1/1");
        when(request.getParameter("endDate")).thenReturn("2/2/2");
        when(request.getParameter("manualCPC")).thenReturn("23.51");
        when(request.getParameter("dailyBudget")).thenReturn("20.12");
        when(request.getParameter("locations")).thenReturn("California Texas");
        when(request.getParameter("domain")).thenReturn("google.com");
        when(request.getParameter("targets")).thenReturn("test1.com test2.com");
        when(request.getParameter("adText")).thenReturn("sample ad text");
        when(request.getParameter("impressions")).thenReturn("12412");
        when(request.getParameter("clicks")).thenReturn("535");
        when(request.getParameter("cost")).thenReturn("2145.50");

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        assertEquals(0, ds.prepare(new Query("DSACampaign")).countEntities(withLimit(10)));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        DSACampaignsServlet servlet = new DSACampaignsServlet();
        servlet.doPost(request, response);

        Query query = new Query("DSACampaign");
    	Entity entity = ds.prepare(query).asSingleEntity();
        assertEquals("3", (String) entity.getProperty("DSACampaignId"));
        assertEquals("2", (String) entity.getProperty("userId"));
        assertEquals("1", (String) entity.getProperty("keywordCampaignId"));
        assertEquals("Test DSA Campaign", (String) entity.getProperty("name"));
        assertEquals("complete", (String) entity.getProperty("campaignStatus"));
        assertEquals("1/1/1", (String) entity.getProperty("startDate"));
        assertEquals("2/2/2", (String) entity.getProperty("endDate"));
        assertEquals(23.51, (double) entity.getProperty("manualCPC"), .01);
        assertEquals(20.12, (double) entity.getProperty("dailyBudget"), .01);
        assertEquals("California Texas", (String) entity.getProperty("locations"));
        assertEquals("google.com", (String) entity.getProperty("domain"));
        assertEquals("test1.com test2.com", (String) entity.getProperty("targets"));
        assertEquals("sample ad text", (String) entity.getProperty("adText"));
        assertEquals(12412, (int) ((long) entity.getProperty("impressions")));
        assertEquals(535, (int) ((long) entity.getProperty("clicks")));
        assertEquals(2145.5, (double) entity.getProperty("cost"), .01);
    }
}