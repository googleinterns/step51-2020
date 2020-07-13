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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
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
        String correspondingKeywordCampaignId = request.getParameter("keywordCampaignId");

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
        UserService userService = UserServiceFactory.getUserService();

        if (userService.isUserLoggedIn()) {
            String userId = userService.getCurrentUser().getUserId();
            String DSACampaignId = request.getParameter("DSACampaignId");
            String keywordCampaignId = request.getParameter("keywordCampaignId");
            DSACampaignId = assignUniqueCampaignId(keywordCampaignId, DSACampaignId);
            
            DSACampaign DSACampaignObject = new DSACampaign(DSACampaignId, userId, keywordCampaignId,
                request.getParameter("name"), request.getParameter("campaignStatus"), request.getParameter("startDate"), request.getParameter("endDate"), 
                Double.parseDouble(request.getParameter("manualCPC")), Double.parseDouble(request.getParameter("dailyBudget")), request.getParameter("locations"),
                request.getParameter("domain"), request.getParameter("targets"), request.getParameter("adText"), Integer.parseInt(request.getParameter("impressions")),
                Integer.parseInt(request.getParameter("clicks")), Double.parseDouble(request.getParameter("cost")));

            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            datastore.put(createEntityFromDSACampaign(DSACampaignObject));

            response.sendRedirect("/Compare/compare.html");
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
        String domain = (String) entity.getProperty("domain");
        String targets = (String) entity.getProperty("targets");
        String adText = (String) entity.getProperty("adText");

        int impressions = (int) ((long) entity.getProperty("impressions"));
        int clicks = (int) ((long) entity.getProperty("clicks"));
        double cost = (double) entity.getProperty("cost");

        return new DSACampaign(DSACampaignId, userId, keywordCampaignId, name, campaignStatus, startDate, endDate, manualCPC, dailyBudget, locations, domain, targets, adText, impressions, clicks, cost);
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
        DSACampaignEntity.setProperty("domain", DSACampaignObject.domain);
        DSACampaignEntity.setProperty("targets", DSACampaignObject.targets);
        DSACampaignEntity.setProperty("adText", DSACampaignObject.adText);
       
        DSACampaignEntity.setProperty("impressions", DSACampaignObject.impressions);
        DSACampaignEntity.setProperty("clicks", DSACampaignObject.clicks);
        DSACampaignEntity.setProperty("cost", DSACampaignObject.cost);

        return DSACampaignEntity;
    }

    /**
     * assignUniqueCampaignId takes in a keyword campaign and the proposed dsaCampaignId and
     * checks with datastore to determine if dsaCampaignId already exists. If campaign ID already exists, 
     * then this method returns the highest existing DSA campaign ID plus one.
     *
     * @param correspondingKeywordCampaignId keyword campaign id associated with the DSA campaign.
     * @param dsaCampaignId                  The proposed DSA campaign ID.
     * @return                               String representing available dsa campaign id.
     */
    public static String assignUniqueCampaignId(String correspondingKeywordCampaignId, String dsaCampaignId) {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Query query = new Query("DSACampaign").setFilter(new Query.FilterPredicate("DSACampaignId", Query.FilterOperator.EQUAL, dsaCampaignId));
      PreparedQuery results = datastore.prepare(query);
      if (results.countEntities(FetchOptions.Builder.withLimit(1)) > 0) {
        query = new Query("DSACampaign").setFilter(new Query.FilterPredicate("keywordCampaignId", Query.FilterOperator.EQUAL, correspondingKeywordCampaignId))
                                        .addSort("DSACampaignId", SortDirection.DESCENDING);
        Entity dsaCampaign = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(1)).get(0);
        DSACampaign highestDSACampaignId = createDSACampaignFromEntity(dsaCampaign);
        int newDSACampaignId = Integer.parseInt(highestDSACampaignId.DSACampaignId) + 1;
        return Integer.toString(newDSACampaignId);
      }
      else {
        return dsaCampaignId;
      }
    }
}