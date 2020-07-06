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

// gets all the keyword campaigns from datastore
// posts new keyword campaigns to datastore
@WebServlet("/keyword-campaigns")
public class KeywordCampaignsServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("keywordCampaign").addSort("keywordCampaignId", SortDirection.ASCENDING);
    	PreparedQuery results = datastore.prepare(query);

        ArrayList<KeywordCampaign> keywordCampaigns = new ArrayList<KeywordCampaign>();
        for (Entity entity : results.asIterable()) {
            int keywordCampaignId = (int) ((long) entity.getProperty("keywordCampaignId"));
            int userId = (int) ((long) entity.getProperty("userId"));
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
            ArrayList<Integer> DSACampaignIds = (ArrayList<Integer>) entity.getProperty("DSACampaignIds");
            KeywordCampaign keywordCampaignObject = new KeywordCampaign(keywordCampaignId, userId, name, fromDate, toDate, dailyBudget, location, domain, target, impressions, clicks, cost, DSACampaignIds);
            keywordCampaigns.add(keywordCampaignObject);
        }

        Gson gson = new Gson();
        String json = gson.toJson(keywordCampaigns);
        response.setContentType("application/json;");
        response.getWriter().println(json);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int keywordCampaignId = Integer.parseInt(request.getParameter("keywordCampaignId"));
        int userId = Integer.parseInt(request.getParameter("userId"));
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

        String DSACampaignIdsStr = request.getParameter("DSACampaignIds");
        String[] DSACampaignIdsArray = DSACampaignIdsStr.split(" ");
        ArrayList<Integer> DSACampaignIds = new ArrayList<Integer>();
        for (String id : DSACampaignIdsArray) {
            DSACampaignIds.add(Integer.parseInt(id));
        }

        Entity keywordCampaignEntity = new Entity("keywordCampaign");
        keywordCampaignEntity.setProperty("keywordCampaignId", keywordCampaignId);
        keywordCampaignEntity.setProperty("userId", userId);
        keywordCampaignEntity.setProperty("name", name);
        keywordCampaignEntity.setProperty("fromDate", fromDate);
        keywordCampaignEntity.setProperty("toDate", toDate);
        keywordCampaignEntity.setProperty("dailyBudget", dailyBudget);
        keywordCampaignEntity.setProperty("location", location);
        keywordCampaignEntity.setProperty("domain", domain);
        keywordCampaignEntity.setProperty("target", target);
        keywordCampaignEntity.setProperty("impressions", impressions);
        keywordCampaignEntity.setProperty("clicks", clicks);
        keywordCampaignEntity.setProperty("cost", cost);
        keywordCampaignEntity.setProperty("DSACampaignIds", DSACampaignIds);
	
    	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(keywordCampaignEntity);

        // used for testing purposes
        response.getWriter().append("The following keyword campaign object was put in datastore: " + keywordCampaignId + " " + userId + " " + name + " " + fromDate
            + " " + toDate + " " + dailyBudget + " " + location + " " + domain + " " + target + " " + impressions + " " + clicks + " " + cost + " " + DSACampaignIdsStr);
            
        response.sendRedirect("/Compare/compare.html");
    }
}