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
import com.google.appengine.api.datastore.Query.SortDirection;
import java.io.IOException;
import java.io.PrintWriter;
import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// This servlet is responsible for handling campaign preset data for each user.
@WebServlet("/preset")
public class PresetServlet extends HttpServlet {

    //TODO: write preset data GET and POST request handlers
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        /*
      // parse each parameter from the save preset form.
      String userEmail = request.getParameter("userEmail");
      String userId = request.getParameter("userId");
      String presetId = request.getParameter("presetId");
      String simName = request.getParameter("sim_name");
      String fromDate = request.getParameter("start_date");
      String toDate = request.getParameter("end_date");
      double dailyBudget = Double.parseDouble(request.getParameter("daily_budget"));
      String location = request.getParameter("location");
      String language = request.getParameter("language");
      String domain = request.getParameter("domain");
      String target = request.getParameter("target_page");
      String adText = request.getParameter("ad_text");
      double cpc = Double.parseDouble(request.getParameter("cpc"));

      Entity presetEntity = new Entity("PresetData");      
      presetEntity.setProperty("userEmail", userEmail);
      presetEntity.setProperty("presetId", presetId);
      DSACampaign dsaCampaign = new DSACampaign(0, userId, 0, simName, fromDate, toDate, dailyBudget, location, domain, target);
      Gson gson = new Gson();
      String dsaCampaignData = gson.toJson(dsaCampaign);
      presetEntity.setProperty("presetData", dsaCampaignData);
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(presetEntity);
      */
    }
}