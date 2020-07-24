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
        UserService userService = UserServiceFactory.getUserService();

        ArrayList<KeywordCampaign> keywordCampaigns = new ArrayList<KeywordCampaign>();
        if (userService.isUserLoggedIn()) {
            // user ID represents user email
            String userId = userService.getCurrentUser().getEmail();

            // get the keyword campaigns associated with the logged in user from datastore
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Query query = new Query("keywordCampaign").setFilter(new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId)).addSort("keywordCampaignId", SortDirection.ASCENDING);
            PreparedQuery results = datastore.prepare(query);

            for (Entity entity : results.asIterable()) {
                keywordCampaigns.add(createKeywordCampaignFromEntity(entity));
            }
        }
        
        Gson gson = new Gson();
        String json = gson.toJson(keywordCampaigns);
        response.setContentType("application/json;");
        response.getWriter().println(json);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {	
        UserService userService = UserServiceFactory.getUserService();

        if (userService.isUserLoggedIn()) {
            String userId = userService.getCurrentUser().getEmail();

            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            KeywordCampaign keywordCampaignObject = new KeywordCampaign(getNewCampaignId(true), userId, request.getParameter("name"), 
                Double.parseDouble(request.getParameter("manualCPC")), request.getParameter("locations"), request.getParameter("negativeLocations"), 
                Integer.parseInt(request.getParameter("impressions")), Integer.parseInt(request.getParameter("clicks")), Double.parseDouble(request.getParameter("cost")));
            datastore.put(createEntityFromKeywordCampaign(keywordCampaignObject));
                
            response.sendRedirect("/Compare/compare.html");
        } else {
            response.sendRedirect("/index.html");
        }
    }

    public static KeywordCampaign createKeywordCampaignFromEntity(Entity entity) {
        String keywordCampaignId = (String) entity.getProperty("keywordCampaignId");
        String userId = (String) entity.getProperty("userId");

        String name = (String) entity.getProperty("name");
        Double manualCPC = (double) entity.getProperty("manualCPC");
        String locations = (String) entity.getProperty("locations");
        String negativeLocations = (String) entity.getProperty("negativeLocations");

        int impressions = (int) ((long) entity.getProperty("impressions"));
        int clicks = (int) ((long) entity.getProperty("clicks"));
        double cost = (double) entity.getProperty("cost");

        return new KeywordCampaign(keywordCampaignId, userId, name, manualCPC, locations, negativeLocations, impressions, clicks, cost);
    }

    public static Entity createEntityFromKeywordCampaign(KeywordCampaign KeywordCampaign) {
        Entity keywordCampaignEntity = new Entity("keywordCampaign");

        keywordCampaignEntity.setProperty("keywordCampaignId", KeywordCampaign.keywordCampaignId);
        keywordCampaignEntity.setProperty("userId", KeywordCampaign.userId);

        keywordCampaignEntity.setProperty("name", KeywordCampaign.name);
        keywordCampaignEntity.setProperty("manualCPC", KeywordCampaign.manualCPC);
        keywordCampaignEntity.setProperty("locations", KeywordCampaign.locations);
        keywordCampaignEntity.setProperty("negativeLocations", KeywordCampaign.negativeLocations);
       
        keywordCampaignEntity.setProperty("impressions", KeywordCampaign.impressions);
        keywordCampaignEntity.setProperty("clicks", KeywordCampaign.clicks);
        keywordCampaignEntity.setProperty("cost", KeywordCampaign.cost);

        return keywordCampaignEntity;
    }

    // Retrieves a unique keyword campaign id from datastore.
    public static String getNewKeywordCampaignId() {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("numKeywordCampaigns");
        Entity numKeywordCampaignsEntity = datastore.prepare(query).asSingleEntity();
        int numKeywordCampaigns = 1;

        if (numKeywordCampaignsEntity != null) {
            // There are keyword campaigns in datastore, numKeywordCampaignsEntity was already created.
            numKeywordCampaigns = (int) ((long) numKeywordCampaignsEntity.getProperty("number"));
            numKeywordCampaignsEntity.setProperty("number", ++numKeywordCampaigns);
            datastore.put(numKeywordCampaignsEntity);
        } else {
            // There are no keyword campaigns in datastore - need to create numKeywordCampaignsEntity.
            Entity newNumKeywordCampaignsEntity = new Entity("numKeywordCampaigns");
            newNumKeywordCampaignsEntity.setProperty("number", numKeywordCampaigns);
            datastore.put(newNumKeywordCampaignsEntity);
        }

        return Integer.toString(numKeywordCampaigns);
    }

    /*
     * Retrieves a unique campaign id from datastore.
     * If isKeywordCampaign is true, then it will generate the id for a keyword campaign.
     * If false, it will generate the id for a DSA campaign.
     */
    public static String getNewCampaignId(boolean isKeywordCampaign) {
        String queryKind = "numKeywordCampaigns";
        if (!isKeywordCampaign) {
            queryKind = "numDSACampaigns";
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query(queryKind);
        Entity numCampaignsEntity = datastore.prepare(query).asSingleEntity();
        int numCampaigns = 1;

        if (numCampaignsEntity != null) {
            // There are campaigns in datastore, numCampaignsEntity was already created.
            numCampaigns = (int) ((long) numCampaignsEntity.getProperty("number"));
            numCampaignsEntity.setProperty("number", ++numCampaigns);
            datastore.put(numCampaignsEntity);
        } else {
            // There are no campaigns in datastore - need to create numCampaignsEntity.
            Entity newNumCampaignsEntity = new Entity(queryKind);
            newNumCampaignsEntity.setProperty("number", numCampaigns);
            datastore.put(newNumCampaignsEntity);
        }

        return Integer.toString(numCampaigns);
    }
}