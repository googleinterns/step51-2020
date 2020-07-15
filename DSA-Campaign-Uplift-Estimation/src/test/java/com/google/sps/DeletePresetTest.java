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
public final class DeletePresetTest {
  
  @Mock
  HttpServletRequest presetRequest;

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
   * presetServletdoPostTest works by first creating a mock HttpServletRequest
   * object and calling PresetServlet.doPost(). Following, the function has
   * multiple assertEquals to verify that the datastore accurately stores
   * the request data.
   */
  @Test
  public void deletePresetDoPostTest() throws IOException, ServletException {
    presetRequest = mock(HttpServletRequest.class);
    
    response = mock(HttpServletResponse.class);
    
    // parameters used to initialize HttpServletRequest parameters
    when(presetRequest.getParameter("DSACampaignId")).thenReturn("0");
    when(presetRequest.getParameter("campaignStatus")).thenReturn("pending");
    when(presetRequest.getParameter("userId")).thenReturn("0");
    when(presetRequest.getParameter("keywordCampaignId")).thenReturn("0");
    when(presetRequest.getParameter("presetId")).thenReturn("0");
    when(presetRequest.getParameter("name")).thenReturn("Test Name");
    when(presetRequest.getParameter("startDate")).thenReturn("2001-01-01");
    when(presetRequest.getParameter("endDate")).thenReturn("2001-01-03");
    when(presetRequest.getParameter("manualCPC")).thenReturn("0.01");
    when(presetRequest.getParameter("dailyBudget")).thenReturn("34.00");
    when(presetRequest.getParameter("locations")).thenReturn("California, United States of America");
    when(presetRequest.getParameter("domain")).thenReturn("http://google.com");
    when(presetRequest.getParameter("targets")).thenReturn("http://google.com/page1");
    when(presetRequest.getParameter("adText")).thenReturn("Some text");
    when(presetRequest.getParameter("cost")).thenReturn("0");
    when(presetRequest.getParameter("impressions")).thenReturn("0");
    when(presetRequest.getParameter("clicks")).thenReturn("0");

    // verify that datastore is empty
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    assertEquals(0, datastore.prepare(new Query("PresetData")).countEntities(withLimit(10)));
    
    new PresetServlet().doPost(presetRequest, response);

    // verify that there is a preset entry in datastore
    assertEquals(1, datastore.prepare(new Query("PresetData")).countEntities(withLimit(10)));

    Query query = new Query("PresetData");
    Entity entity = datastore.prepare(query).asSingleEntity();
    
    assertEquals(presetRequest.getParameter("userId"), entity.getProperty("userId"));
    assertEquals(presetRequest.getParameter("presetId"), entity.getProperty("presetId"));

    HttpServletRequest deleteRequest = mock(HttpServletRequest.class);

    HttpServletResponse deleteRespose = mock(HttpServletResponse.class);

    when(deleteRequest.getParameter("userId")).thenReturn("0");
    when(deleteRequest.getParameter("presetId")).thenReturn("0");
    new DeletePresetServlet().doPost(deleteRequest, deleteRespose);
    
    assertEquals(0, datastore.prepare(new Query("PresetData")).countEntities(withLimit(10)));
  }
}
