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
            keywordCampaigns.add(createKeywordCampaignFromEntity(entity));
        }

        Gson gson = new Gson();
        String json = gson.toJson(keywordCampaigns);
        response.setContentType("application/json;");
        response.getWriter().println(json);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {	
    	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        KeywordCampaign keywordCampaignObject = new KeywordCampaign(request.getParameter("keywordCampaignId"), request.getParameter("userId"), request.getParameter("DSACampaignIds"), 
            request.getParameter("name"),Integer.parseInt(request.getParameter("impressions")), Integer.parseInt(request.getParameter("clicks")), Double.parseDouble(request.getParameter("cost")));
        datastore.put(createEntityFromKeywordCampaign(keywordCampaignObject));
            
        response.sendRedirect("/Compare/compare.html");
    }

    public static KeywordCampaign createKeywordCampaignFromEntity(Entity entity) {
        String keywordCampaignId = (String) entity.getProperty("keywordCampaignId");
        String userId = (String) entity.getProperty("userId");
        String DSACampaignIds = (String) entity.getProperty("DSACampaignIds");
        String name = (String) entity.getProperty("name");

        int impressions = (int) ((long) entity.getProperty("impressions"));
        int clicks = (int) ((long) entity.getProperty("clicks"));
        double cost = (double) entity.getProperty("cost");

        return new KeywordCampaign(keywordCampaignId, userId, DSACampaignIds, name, impressions, clicks, cost);
    }

    public static Entity createEntityFromKeywordCampaign(KeywordCampaign KeywordCampaign) {
        Entity keywordCampaignEntity = new Entity("keywordCampaign");

        keywordCampaignEntity.setProperty("keywordCampaignId", KeywordCampaign.keywordCampaignId);
        keywordCampaignEntity.setProperty("userId", KeywordCampaign.userId);
        keywordCampaignEntity.setProperty("DSACampaignIds", KeywordCampaign.DSACampaignIds);
        keywordCampaignEntity.setProperty("name", KeywordCampaign.name);
       
        keywordCampaignEntity.setProperty("impressions", KeywordCampaign.impressions);
        keywordCampaignEntity.setProperty("clicks", KeywordCampaign.clicks);
        keywordCampaignEntity.setProperty("cost", KeywordCampaign.cost);

        return keywordCampaignEntity;
    }
}