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

// gets all the DSA campaigns from datastore that correspond to a specified keyword campaign
// posts new DSA campaigns to datastore
@WebServlet("/DSA-campaigns")
public class DSACampaignsServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int correspondingKeywordCampaignId = Integer.parseInt(request.getParameter("keywordCampaignId"));

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("DSACampaign").setFilter(new Query.FilterPredicate("keywordCampaignId", Query.FilterOperator.EQUAL, correspondingKeywordCampaignId)).addSort("DSACampaignId", SortDirection.ASCENDING);
    	PreparedQuery results = datastore.prepare(query);

        ArrayList<DSACampaign> DSACampaigns = new ArrayList<DSACampaign>();
        
        for (Entity entity : results.asIterable()) {
            int DSACampaignId = (int) ((long) entity.getProperty("DSACampaignId"));
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
            DSACampaigns.add(DSACampaignObject);
        }

        Gson gson = new Gson();
        String json = gson.toJson(DSACampaigns);
        response.setContentType("application/json;");
        response.getWriter().println(json);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int DSACampaignId = Integer.parseInt(request.getParameter("DSACampaignId"));
        int userId = Integer.parseInt(request.getParameter("userId"));
        int keywordCampaignId = Integer.parseInt(request.getParameter("keywordCampaignId"));
        String name = request.getParameter("name");
        String fromDate = request.getParameter("fromDate");
        String toDate = request.getParameter("toDate");
        double dailyBudget = Double.parseDouble(request.getParameter("dailyBudget"));
        String location = request.getParameter("location");
        String domain = request.getParameter("domain");
        String target = request.getParameter("target");
        int impressions = Integer.parseInt(request.getParameter("impressions"));
        int clicks = Integer.parseInt(request.getParameter("clicks"));
        double cost = Double.parseDouble(request.getParameter("cost"));

        Entity DSACampaignEntity = new Entity("DSACampaign");
        DSACampaignEntity.setProperty("DSACampaignId", DSACampaignId);
        DSACampaignEntity.setProperty("userId", userId);
        DSACampaignEntity.setProperty("keywordCampaignId", keywordCampaignId);
        DSACampaignEntity.setProperty("name", name);
        DSACampaignEntity.setProperty("fromDate", fromDate);
        DSACampaignEntity.setProperty("toDate", toDate);
        DSACampaignEntity.setProperty("dailyBudget", dailyBudget);
        DSACampaignEntity.setProperty("location", location);
        DSACampaignEntity.setProperty("domain", domain);
        DSACampaignEntity.setProperty("target", target);
        DSACampaignEntity.setProperty("impressions", impressions);
        DSACampaignEntity.setProperty("clicks", clicks);
        DSACampaignEntity.setProperty("cost", cost);
	
    	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(DSACampaignEntity);

        response.sendRedirect("/Compare/compare.html");
    }
}