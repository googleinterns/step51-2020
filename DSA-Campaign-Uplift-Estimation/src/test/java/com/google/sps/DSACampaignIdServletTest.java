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
 * Tests the doGet() function in DSACampaignIdServlet.java.
 */
@RunWith(JUnit4.class)
public final class DSACampaignIdServletTest {
    
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
    public void DSACampaignIdServletDoGet() throws IOException, ServletException {
        when(request.getParameter("DSACampaignIds")).thenReturn("1 2");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Entity DSACampaignEntity1 = new Entity("DSACampaign");
        DSACampaignEntity1.setProperty("DSACampaignId", 1);
        DSACampaignEntity1.setProperty("userId", 2);
        DSACampaignEntity1.setProperty("keywordCampaignId", 1);
        DSACampaignEntity1.setProperty("name", "entity 1");
        DSACampaignEntity1.setProperty("fromDate", "1/1/1");
        DSACampaignEntity1.setProperty("toDate", "2/2/2");
        DSACampaignEntity1.setProperty("dailyBudget", 123.2);
        DSACampaignEntity1.setProperty("location", "CA");
        DSACampaignEntity1.setProperty("domain", "google.com");
        DSACampaignEntity1.setProperty("target", "google.com");
        DSACampaignEntity1.setProperty("impressions", 432);
        DSACampaignEntity1.setProperty("clicks", 123);
        DSACampaignEntity1.setProperty("cost", 42.51);
        ds.put(DSACampaignEntity1);

        Entity DSACampaignEntity2 = new Entity("DSACampaign");
        DSACampaignEntity2.setProperty("DSACampaignId", 2);
        DSACampaignEntity2.setProperty("userId", 2);
        DSACampaignEntity2.setProperty("keywordCampaignId", 1);
        DSACampaignEntity2.setProperty("name", "entity 2");
        DSACampaignEntity2.setProperty("fromDate", "1/1/1");
        DSACampaignEntity2.setProperty("toDate", "2/2/2");
        DSACampaignEntity2.setProperty("dailyBudget", 123.2);
        DSACampaignEntity2.setProperty("location", "CA");
        DSACampaignEntity2.setProperty("domain", "google.com");
        DSACampaignEntity2.setProperty("target", "google.com");
        DSACampaignEntity2.setProperty("impressions", 432);
        DSACampaignEntity2.setProperty("clicks", 123);
        DSACampaignEntity2.setProperty("cost", 42.51);
        ds.put(DSACampaignEntity2);

        DSACampaignIdServlet servlet = new DSACampaignIdServlet();
        servlet.doGet(request, response);
        String result = sw.getBuffer().toString().trim();
        String expectedStr = "[{\"DSACampaignId\":1,\"userId\":2,\"keywordCampaignId\":1,\"name\":\"entity 1\",\"fromDate\":\"1/1/1\",\"toDate\":\"2/2/2\",";
        expectedStr += "\"dailyBudget\":123.2,\"location\":\"CA\",\"domain\":\"google.com\",\"target\":\"google.com\",\"impressions\":432,\"clicks\":123,\"cost\":42.51},";
        expectedStr += "{\"DSACampaignId\":2,\"userId\":2,\"keywordCampaignId\":1,\"name\":\"entity 2\",\"fromDate\":\"1/1/1\",\"toDate\":\"2/2/2\",";
        expectedStr += "\"dailyBudget\":123.2,\"location\":\"CA\",\"domain\":\"google.com\",\"target\":\"google.com\",\"impressions\":432,\"clicks\":123,\"cost\":42.51}]";
        assertEquals(new String(expectedStr), result);
    }
}