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
import java.util.ArrayList;
import com.google.sps.classes.CampaignPreset;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.*;
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
import static org.mockito.Mockito.mock;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.google.gson.Gson;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * Tests the doGet() and doPost() functions in DSACampaignsServlet.java.
 */
@RunWith(JUnit4.class)
public final class PresetServletTest {
  
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

  /**
   * presetServletDoGetTest tests the doGet function of PresetServlet.
   * Due to the security restrictions of cloud shell, this function
   * is not able to directly test the doGet function, however, it mimicks
   * and tests the logic that is used in doGet. A successful test is determined
   * comparing the number of objects being parsed by doGet. In this context,
   * one object is added to datastore, thus doGet must contain one object.
   */
  @Test
  public void presetServletDoGetTest() throws IOException, ServletException {
    request = mock(HttpServletRequest.class);
    
    response = mock(HttpServletResponse.class);

    // parameters used to initialize HttpServletRequest parameters
    when(request.getParameter("DSACampaignId")).thenReturn("0");
    when(request.getParameter("campaignStatus")).thenReturn("pending");
    when(request.getParameter("userId")).thenReturn("0");
    when(request.getParameter("keywordCampaignId")).thenReturn("0");
    when(request.getParameter("presetId")).thenReturn("0");
    when(request.getParameter("name")).thenReturn("Test Name");
    when(request.getParameter("startDate")).thenReturn("2001-01-01");
    when(request.getParameter("endDate")).thenReturn("2001-01-03");
    when(request.getParameter("manualCPC")).thenReturn("0.01");
    when(request.getParameter("dailyBudget")).thenReturn("34.00");
    when(request.getParameter("locations")).thenReturn("California, United States of America");
    when(request.getParameter("domain")).thenReturn("http://google.com");
    when(request.getParameter("targets")).thenReturn("http://google.com/page1");
    when(request.getParameter("adText")).thenReturn("Some text");
    when(request.getParameter("cost")).thenReturn("0");
    when(request.getParameter("impressions")).thenReturn("0");
    when(request.getParameter("clicks")).thenReturn("0");  

    // verify that datastore is empty
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    assertEquals(0, datastore.prepare(new Query("PresetData")).countEntities(withLimit(10)));
    
    new PresetServlet().doPost(request, response);

    String userId = request.getParameter("userId");
    Filter propertyFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);
    Query query = new Query("PresetData").setFilter(propertyFilter).addSort("timestamp", SortDirection.ASCENDING);;
    PreparedQuery results = datastore.prepare(query);
    ArrayList<CampaignPreset> presets = new ArrayList<>();
    
    for (Entity currEntry : results.asIterable()) {
      String jsonData = (String) currEntry.getProperty("presetData");
      userId = ((String) currEntry.getProperty("userId")).equals(userId) ? userId : null;
      String presetId = (String) currEntry.getProperty("presetId");
      
      DSACampaign presetData = new Gson().fromJson(jsonData, DSACampaign.class);
      CampaignPreset campaignPreset = new CampaignPreset(userId, presetId, presetData);

      presets.add(campaignPreset);
    }

    Gson gson = new Gson();
		String jsonData = gson.toJson(presets);

    // only expecting one object from datastore.
    assertEquals(1, presets.size());
  }

  /**
   * presetServletdoPostTest works by first creating a mock HttpServletRequest
   * object and calling PresetServlet.doPost(). Following, the function has
   * multiple assertEquals to verify that the datastore accurately stores
   * the request data.
   */
  @Test
  public void presetServletdoPostTest() throws IOException, ServletException {
    request = mock(HttpServletRequest.class);
    
    response = mock(HttpServletResponse.class);
    
    // parameters used to initialize HttpServletRequest parameters
    when(request.getParameter("DSACampaignId")).thenReturn("0");
    when(request.getParameter("campaignStatus")).thenReturn("pending");
    when(request.getParameter("userId")).thenReturn("0");
    when(request.getParameter("keywordCampaignId")).thenReturn("0");
    when(request.getParameter("presetId")).thenReturn("0");
    when(request.getParameter("name")).thenReturn("Test Name");
    when(request.getParameter("startDate")).thenReturn("2001-01-01");
    when(request.getParameter("endDate")).thenReturn("2001-01-03");
    when(request.getParameter("manualCPC")).thenReturn("0.01");
    when(request.getParameter("dailyBudget")).thenReturn("34.00");
    when(request.getParameter("locations")).thenReturn("California, United States of America");
    when(request.getParameter("domain")).thenReturn("http://google.com");
    when(request.getParameter("targets")).thenReturn("http://google.com/page1");
    when(request.getParameter("adText")).thenReturn("Some text");
    when(request.getParameter("cost")).thenReturn("0");
    when(request.getParameter("impressions")).thenReturn("0");
    when(request.getParameter("clicks")).thenReturn("0");

    // verify that datastore is empty
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    assertEquals(0, datastore.prepare(new Query("PresetData")).countEntities(withLimit(10)));
    
    new PresetServlet().doPost(request, response);

    // verify that there is a preset entry in datastore
    assertEquals(1, datastore.prepare(new Query("PresetData")).countEntities(withLimit(10)));

    Query query = new Query("PresetData");
    Entity entity = datastore.prepare(query).asSingleEntity();
    
    assertEquals(request.getParameter("userId"), entity.getProperty("userId"));
    assertEquals(request.getParameter("presetId"), entity.getProperty("presetId"));

    // convert JSON data representing DSACampaign to DSACampaign object
    Gson gson = new Gson();
    String dsaCampaignData = (String) entity.getProperty("presetData");
    DSACampaign testDSACampaign = gson.fromJson(dsaCampaignData, DSACampaign.class);

    // verify that the data used to create the campaign is accurately sent and retrieved from datastore
    assertEquals(request.getParameter("keywordCampaignId"), testDSACampaign.keywordCampaignId);
    assertEquals(request.getParameter("DSACampaignId"), testDSACampaign.DSACampaignId);
    assertEquals(request.getParameter("name"), testDSACampaign.name);
    assertEquals(request.getParameter("locations"), testDSACampaign.locations);
    assertEquals(request.getParameter("startDate"), testDSACampaign.startDate);
    assertEquals(request.getParameter("endDate"), testDSACampaign.endDate);
    assertEquals(Double.parseDouble(request.getParameter("manualCPC")), testDSACampaign.manualCPC, 0.00);
    assertEquals(Double.parseDouble(request.getParameter("dailyBudget")), testDSACampaign.dailyBudget, 0.00);
    assertEquals(request.getParameter("domain"), testDSACampaign.domain);
    assertEquals(request.getParameter("targets"), testDSACampaign.targets);
    assertEquals(request.getParameter("adText"), testDSACampaign.adText);
    assertEquals(Double.parseDouble(request.getParameter("cost")), testDSACampaign.cost, 0.0);
    assertEquals(Integer.parseInt(request.getParameter("impressions")), testDSACampaign.impressions, 0);
    assertEquals(Integer.parseInt(request.getParameter("clicks")), testDSACampaign.clicks, 0);
  }
}