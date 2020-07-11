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

import com.google.sps.classes.*;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.*;
import java.io.IOException;
import java.io.PrintWriter;
import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.*;
import java.util.ArrayList;

// This servlet is responsible for handling campaign preset data for each user.
@WebServlet("/preset")
public class PresetServlet extends HttpServlet {

  //TODO: write preset data GET and POST request handlers
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String userId = request.getParameter("userId");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
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
		
		response.setContentType("application/json;");
		response.getWriter().println(jsonData);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    // parse each parameter from the save preset form.
    String userId = request.getParameter("userId");
    String presetId = request.getParameter("presetId");
    String name = request.getParameter("name");
    String startDate = request.getParameter("startDate");
    String endDate = request.getParameter("endDate");
    double dailyBudget = Double.parseDouble(request.getParameter("dailyBudget"));
    String locations = request.getParameter("locations");
    String domain = request.getParameter("domain");
    String targets = request.getParameter("targets");
    String adText = request.getParameter("adText");
    double manualCPC = Double.parseDouble(request.getParameter("manualCPC"));

    Entity presetEntity = new Entity("PresetData");      
    presetEntity.setProperty("userId", userId);
    presetEntity.setProperty("presetId", presetId);
    presetEntity.setProperty("timestamp", System.currentTimeMillis());

    DSACampaign dsaCampaign = new DSACampaign("0", userId, "0", name, "pending", startDate, endDate, manualCPC, dailyBudget, locations, domain, targets, adText, 0, 0, 0);
    Gson gson = new Gson();
    String dsaCampaignData = gson.toJson(dsaCampaign);
    presetEntity.setProperty("presetData", dsaCampaignData);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(presetEntity);
  }
}