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
            String keywordCampaignId = (String) entity.getProperty("keywordCampaignId");
            String userId = (String) entity.getProperty("userId");
            ArrayList<String> DSACampaignIds = (ArrayList<String>) entity.getProperty("DSACampaignIds");
            String name = (String) entity.getProperty("name");

            int impressions = (int) ((long) entity.getProperty("impressions"));
            int clicks = (int) ((long) entity.getProperty("clicks"));
            double cost = (double) entity.getProperty("cost");

            KeywordCampaign keywordCampaignObject = new KeywordCampaign(keywordCampaignId, userId, DSACampaignIds, name, impressions, clicks, cost);
            keywordCampaigns.add(keywordCampaignObject);
        }

        Gson gson = new Gson();
        String json = gson.toJson(keywordCampaigns);
        response.setContentType("application/json;");
        response.getWriter().println(json);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String keywordCampaignId = request.getParameter("keywordCampaignId");
        String userId = request.getParameter("userId");

        String[] DSACampaignIdsArray = request.getParameter("DSACampaignIds").split(" ");
        ArrayList<String> DSACampaignIds = new ArrayList<String>();
        for (String id : DSACampaignIdsArray) {
            DSACampaignIds.add(id);
        }

        String name = request.getParameter("name");

        int impressions = Integer.parseInt(request.getParameter("impressions"));
        int clicks = Integer.parseInt(request.getParameter("clicks"));
        double cost = Double.parseDouble(request.getParameter("cost"));

        Entity keywordCampaignEntity = new Entity("keywordCampaign");
        keywordCampaignEntity.setProperty("keywordCampaignId", keywordCampaignId);
        keywordCampaignEntity.setProperty("userId", userId);
        keywordCampaignEntity.setProperty("DSACampaignIds", DSACampaignIds);
        keywordCampaignEntity.setProperty("name", name);
       
        keywordCampaignEntity.setProperty("impressions", impressions);
        keywordCampaignEntity.setProperty("clicks", clicks);
        keywordCampaignEntity.setProperty("cost", cost);
	
    	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(keywordCampaignEntity);
            
        response.sendRedirect("/Compare/compare.html");
    }
}