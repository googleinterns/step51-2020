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
import java.util.ArrayList;

// gets the DSA campaigns from datastore corresponding to the DSA campaign id's in the array from the request
@WebServlet("/DSA-campaign-id")
public class DSACampaignIdServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] DSACampaignIdStrings = request.getParameter("DSACampaignIds").split(" ");
        int[] DSACampaignIdList = new int[DSACampaignIdStrings.length];
        for (int i=0; i<DSACampaignIdStrings.length; i++) {
            DSACampaignIdList[i] = Integer.parseInt(DSACampaignIdStrings[i]);
        }

        ArrayList<DSACampaign> DSACampaignsList = new ArrayList<DSACampaign>();

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        for (int DSACampaignId : DSACampaignIdList) {
            Query query = new Query("DSACampaign").setFilter(new Query.FilterPredicate("DSACampaignId", Query.FilterOperator.EQUAL, DSACampaignId));
            Entity entity = datastore.prepare(query).asSingleEntity();

            int userId = (int) ((long) entity.getProperty("userId"));
            int keywordCampaignId = (int) ((long) entity.getProperty("keywordCampaignId"));
            String name = (String) entity.getProperty("name");
            String fromDate = (String) entity.getProperty("fromDate");
            String toDate = (String) entity.getProperty("toDate");
            double dailyBudget = (double) entity.getProperty("dailyBudget");
            String location = (String) entity.getProperty("location");
            String domain = (String) entity.getProperty("domain");
            String target = (String) entity.getProperty("target");
            int impressions = (int) ((long) entity.getProperty("impressions"));
            int clicks = (int) ((long) entity.getProperty("clicks"));
            double cost = (double) entity.getProperty("cost");
            DSACampaign DSACampaignObject = new DSACampaign(DSACampaignId, userId, keywordCampaignId, name, fromDate, toDate, dailyBudget, location, domain, target, impressions, clicks, cost);
            DSACampaignsList.add(DSACampaignObject);
        }

        Gson gson = new Gson();
        String json = gson.toJson(DSACampaignsList);
        response.setContentType("application/json;");
        response.getWriter().println(json);
    }
}