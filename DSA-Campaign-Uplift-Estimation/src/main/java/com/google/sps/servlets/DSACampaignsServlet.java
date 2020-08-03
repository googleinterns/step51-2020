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

// gets all the DSA campaigns from datastore that correspond to a specified keyword campaign
// posts new DSA campaigns to datastore
@WebServlet("/DSA-campaigns")
public class DSACampaignsServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String correspondingQueryId = request.getParameter("keywordCampaignId");
        String queryName = "keywordCampaignId";

        if (request.getParameter("hangouts") != null) {
          correspondingQueryId = request.getParameter("userId");
          queryName = "userId";
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("DSACampaign").setFilter(new Query.FilterPredicate(queryName, Query.FilterOperator.EQUAL, correspondingQueryId)).addSort("DSACampaignId", SortDirection.ASCENDING);
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
        boolean hangoutsRequest = request.getParameter("hangouts") != null;
        UserService userService = UserServiceFactory.getUserService();

        if (request.getParameter("delete") != null) {
            System.err.println("Inside If Parameter");
            String campaignId = request.getParameter("id");
            deleteDSACampaign(campaignId);
            return;
        } else if (userService.isUserLoggedIn()) {
            // user ID represents user email
            String userId = userService.getCurrentUser().getEmail();
            
            DSACampaign DSACampaignObject = new DSACampaign(KeywordCampaignsServlet.getNewCampaignId(false), userId, request.getParameter("keywordCampaignId"),
                request.getParameter("name"), "pending", request.getParameter("startDate"), request.getParameter("endDate"), 
                Double.parseDouble(request.getParameter("manualCPC")), Double.parseDouble(request.getParameter("dailyBudget")), request.getParameter("locations"),
                request.getParameter("negativeLocations"), request.getParameter("domain"), request.getParameter("targets"), request.getParameter("adText"), 
                0, 0, 0, null);

            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Entity DSACampaignEntity = createEntityFromDSACampaign(DSACampaignObject);
            datastore.put(DSACampaignEntity);

            PendingCampaignsExistServlet.changePendingCampaignsExistStatus(1);

            if (!hangoutsRequest) {
              response.sendRedirect("/Home/home.html");
            }
        } else {
            response.sendRedirect("/index.html");
        }
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
        String locations = (String) entity.getProperty("locations");
        String negativeLocations = (String) entity.getProperty("negativeLocations");
        String domain = (String) entity.getProperty("domain");
        String targets = (String) entity.getProperty("targets");
        String adText = (String) entity.getProperty("adText");

        int impressions = (int) ((long) entity.getProperty("impressions"));
        int clicks = (int) ((long) entity.getProperty("clicks"));
        double cost = (double) entity.getProperty("cost");
        String[][] SQR = createSQRFromEntity((EmbeddedEntity) entity.getProperty("SQR"));

        return new DSACampaign(DSACampaignId, userId, keywordCampaignId, name, campaignStatus, startDate, endDate, manualCPC, dailyBudget, locations, negativeLocations, domain, targets, adText, impressions, clicks, cost, SQR);
    }

    public static Entity createEntityFromDSACampaign(DSACampaign DSACampaignObject) {
        Entity DSACampaignEntity = new Entity("DSACampaign");
        DSACampaignEntity.setProperty("DSACampaignId", DSACampaignObject.DSACampaignId);

        DSACampaignEntity.setProperty("userId", DSACampaignObject.userId);
        DSACampaignEntity.setProperty("keywordCampaignId", DSACampaignObject.keywordCampaignId);

        DSACampaignEntity.setProperty("name", DSACampaignObject.name);
        DSACampaignEntity.setProperty("campaignStatus", DSACampaignObject.campaignStatus);
        DSACampaignEntity.setProperty("startDate", DSACampaignObject.startDate);
        DSACampaignEntity.setProperty("endDate", DSACampaignObject.endDate);
        DSACampaignEntity.setProperty("manualCPC", DSACampaignObject.manualCPC);
        DSACampaignEntity.setProperty("dailyBudget", DSACampaignObject.dailyBudget);
        DSACampaignEntity.setProperty("locations", DSACampaignObject.locations);
        DSACampaignEntity.setProperty("negativeLocations", DSACampaignObject.negativeLocations);
        DSACampaignEntity.setProperty("domain", DSACampaignObject.domain);
        DSACampaignEntity.setProperty("targets", DSACampaignObject.targets);
        DSACampaignEntity.setProperty("adText", DSACampaignObject.adText);
       
        DSACampaignEntity.setProperty("impressions", DSACampaignObject.impressions);
        DSACampaignEntity.setProperty("clicks", DSACampaignObject.clicks);
        DSACampaignEntity.setProperty("cost", DSACampaignObject.cost);

        return DSACampaignEntity;
    }

    public static String[][] createSQRFromEntity(EmbeddedEntity SQREmbeddedEntity) {
        if (SQREmbeddedEntity == null) {
            return null;
        }

        int numLines = (int) ((long) SQREmbeddedEntity.getProperty("numLines"));

        // col 1 = queries; col 2 = urls
        String[][] SQRArr = new String[numLines][2];

        for (int lineNum=1; lineNum<=numLines; lineNum++) {
            String propertyName = "Line " + lineNum;

            SQRArr[lineNum-1][0] = (String) SQREmbeddedEntity.getProperty(propertyName + " Query");
            SQRArr[lineNum-1][1] = (String) SQREmbeddedEntity.getProperty(propertyName + " URL");
        }

        return SQRArr;
    }

    public void deleteDSACampaign(String campaignId) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query query =
            new Query("DSACampaign")
                .setFilter(new Query.FilterPredicate("DSACampaignId", Query.FilterOperator.EQUAL, campaignId));
        Entity entity = datastore.prepare(query).asSingleEntity();

        long entityId = entity.getKey().getId();

        System.err.println("Deleting this ID:" + entityId);
        Key taskEntityKey = KeyFactory.createKey("DSACampaign", entityId);
        datastore.delete(taskEntityKey);
    }
}