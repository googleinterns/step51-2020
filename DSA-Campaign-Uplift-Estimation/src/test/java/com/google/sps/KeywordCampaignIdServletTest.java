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
 * Tests the doGet() function in KeywordCampaignIdServlet.java.
 */
@RunWith(JUnit4.class)
public final class KeywordCampaignIdServletTest {
    
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
    public void keywordCampaignIdServletDoGet() throws IOException, ServletException {
        when(request.getParameter("keywordCampaignId")).thenReturn("1");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        KeywordCampaign keywordCampaignObject = new KeywordCampaign("1", "2", "4, 2, 5", "entity 1", 432, 123, 42.51);
        ds.put(KeywordCampaignsServlet.createEntityFromKeywordCampaign(keywordCampaignObject));
        assertEquals(1, ds.prepare(new Query("keywordCampaign")).countEntities(withLimit(10)));

        KeywordCampaignIdServlet servlet = new KeywordCampaignIdServlet();
        servlet.doGet(request, response);
        String result = sw.getBuffer().toString().trim();
        String expectedStr = "{\"keywordCampaignId\":\"1\",\"userId\":\"2\",\"DSACampaignIds\":\"4, 2, 5\",\"name\":\"entity 1\",";
        expectedStr += "\"impressions\":432,\"clicks\":123,\"cost\":42.51}";
        assertEquals(new String(expectedStr), result);
    }
}