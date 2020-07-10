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
import com.google.gson.Gson;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

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

  @Test
  public void presetServletDoGetTest() throws IOException, ServletException {

  }

  /**
   * presetServletdoPostTest works by first mimicking the behavior of PresetServlet.doPost().
   * and verifying that it is able to successfully post data to the datastore.
   */
  @Test
  public void presetServletdoPostTest() throws IOException, ServletException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    assertEquals(0, datastore.prepare(new Query("PresetData")).countEntities(withLimit(10)));
    Entity presetEntity = new Entity("PresetData"); 
    String userId = "0";
    String presetId = "0";
    String name = "Test Name";
    // date format - yyyy-mm-dd
    String startDate = "2001-01-01";
    String endDate = "2001-01-03";
    double manualCPC = 0.01;
    double dailyBudget = 34.00;
    String locations = "California, United States of America";
    String domain = "http://google.com";
    String targets = "http://google.com/page1";
    String adText = "Test ad text";

    presetEntity.setProperty("userId", userId);
    presetEntity.setProperty("presetId", presetId);

    DSACampaign dsaCampaign = new DSACampaign("0", userId, "0", name, "pending", startDate, endDate, manualCPC, dailyBudget, locations, domain, targets, adText, 0, 0, 0);
    Gson gson = new Gson();
    String dsaCampaignData = gson.toJson(dsaCampaign);
    presetEntity.setProperty("presetData", dsaCampaignData);
    datastore.put(presetEntity);
    assertEquals(1, datastore.prepare(new Query("PresetData")).countEntities(withLimit(10)));

    Query query = new Query("PresetData");
    Entity entity = datastore.prepare(query).asSingleEntity();

    assertEquals(userId, entity.getProperty("userId"));
    assertEquals(presetId, entity.getProperty("presetId"));
    assertEquals(dsaCampaignData, entity.getProperty("presetData"));
    DSACampaign testDSACampaign = gson.fromJson(dsaCampaignData, DSACampaign.class);
    assertEquals(dsaCampaign.name, testDSACampaign.name);
    assertEquals(dsaCampaign.locations, testDSACampaign.locations);
    assertEquals(dsaCampaign.startDate, testDSACampaign.startDate);
    assertEquals(dsaCampaign.endDate, testDSACampaign.endDate);
    assertEquals(dsaCampaign.manualCPC, testDSACampaign.manualCPC, 0.01);
    assertEquals(dsaCampaign.dailyBudget, testDSACampaign.dailyBudget, 0.01);
    assertEquals(dsaCampaign.domain, testDSACampaign.domain);
    assertEquals(dsaCampaign.targets, testDSACampaign.targets);
    assertEquals(dsaCampaign.adText, testDSACampaign.adText);
  }
}