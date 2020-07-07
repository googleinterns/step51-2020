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
            DSACampaigns.add(createDSACampaignFromEntity(entity));
        }

        Gson gson = new Gson();
        String json = gson.toJson(DSACampaigns);
        response.setContentType("application/json;");
        response.getWriter().println(json);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Entity DSACampaignEntity = new Entity("DSACampaign");
        DSACampaignEntity.setProperty("DSACampaignId", request.getParameter("DSACampaignId"));
        DSACampaignEntity.setProperty("userId", request.getParameter("userId"));
        DSACampaignEntity.setProperty("keywordCampaignId", request.getParameter("keywordCampaignId"));

        DSACampaignEntity.setProperty("name", request.getParameter("name"));
        DSACampaignEntity.setProperty("campaignStatus", request.getParameter("campaignStatus"));
        DSACampaignEntity.setProperty("startDate", request.getParameter("startDate"));
        DSACampaignEntity.setProperty("endDate", request.getParameter("endDate"));
        DSACampaignEntity.setProperty("dailyBudget", Double.parseDouble(request.getParameter("dailyBudget")));
        DSACampaignEntity.setProperty("locations", request.getParameter("locations").split(" "));
        DSACampaignEntity.setProperty("domain", request.getParameter("domain"));
        DSACampaignEntity.setProperty("targets", request.getParameter("targets").split(" "));
        DSACampaignEntity.setProperty("impressions", Integer.parseInt(request.getParameter("impressions")));
        DSACampaignEntity.setProperty("clicks", Integer.parseInt(request.getParameter("clicks")));
        DSACampaignEntity.setProperty("cost", Double.parseDouble(request.getParameter("cost")));
	
    	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(DSACampaignEntity);

        response.sendRedirect("/Compare/compare.html");
    }

    public static DSACampaign createDSACampaignFromEntity(Entity entity) {
        String DSACampaignId = (String) entity.getProperty("DSACampaignId");
        String userId = (String) entity.getProperty("userId");
        String keywordCampaignId = (String) entity.getProperty("keywordCampaignId");

        String name = (String) entity.getProperty("name");
        String campaignStatus = (String) entity.getProperty("campaignStatus");
        String startDate = (String) entity.getProperty("startDate");
        String endDate = (String) entity.getProperty("endDate");
        double manualCPC = (double) entity.getProperty("manualCPC");
        double dailyBudget = (double) entity.getProperty("dailyBudget");
        String[] locations = (String[]) entity.getProperty("locations");
        String domain = (String) entity.getProperty("domain");
        String[] targets = (String[]) entity.getProperty("targets");
        String adText = (String) entity.getProperty("adText");

        int impressions = (int) ((long) entity.getProperty("impressions"));
        int clicks = (int) ((long) entity.getProperty("clicks"));
        double cost = (double) entity.getProperty("cost");

        return new DSACampaign(DSACampaignId, userId, keywordCampaignId, name, campaignStatus, startDate, endDate, dailyBudget, manualCPC, locations, domain, targets, adText, impressions, clicks, cost);
    }
}