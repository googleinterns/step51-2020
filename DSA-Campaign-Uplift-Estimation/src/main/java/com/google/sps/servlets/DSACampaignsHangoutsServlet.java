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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.classes.DSACampaign;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import java.io.IOException;
import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import com.google.sps.servlets.DSACampaignsServlet;

@WebServlet("/DSA-campaigns-hangouts")
public class DSACampaignsHangoutsServlet extends HttpServlet {
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      String userId = request.getParameter("userId");

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Query query = new Query("DSACampaign").setFilter(new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId)).addSort("DSACampaignId", SortDirection.ASCENDING);
    	PreparedQuery results = datastore.prepare(query);

      ArrayList<DSACampaign> DSACampaigns = new ArrayList<DSACampaign>();
      for (Entity entity : results.asIterable()) {
          DSACampaigns.add(new DSACampaignsServlet().createDSACampaignFromEntity(entity));
      }

      Gson gson = new Gson();
      String json = gson.toJson(DSACampaigns);
      response.setContentType("application/json;");
      response.getWriter().println(json);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("POST request received from bot.");
        if (request.getParameter("delete") != null) {
            System.err.println("Inside If Parameter");
            String campaignId = request.getParameter("id");
            new DSACampaignsServlet().deleteDSACampaign(campaignId);
            return;
        } else {
            // user ID represents user email
            String userId = request.getParameter("userId");
            DSACampaign DSACampaignObject = new DSACampaign(KeywordCampaignsServlet.getNewCampaignId(false), userId, request.getParameter("keywordCampaignId"),
                request.getParameter("name"), "pending", request.getParameter("startDate"), request.getParameter("endDate"), 
                Double.parseDouble(request.getParameter("manualCPC")), Double.parseDouble(request.getParameter("dailyBudget")), request.getParameter("locations"),
                request.getParameter("negativeLocations"), request.getParameter("domain"), request.getParameter("targets"), request.getParameter("adText"), 
                0, 0, 0, null);

            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            datastore.put(new DSACampaignsServlet().createEntityFromDSACampaign(DSACampaignObject));

        }
    }
}