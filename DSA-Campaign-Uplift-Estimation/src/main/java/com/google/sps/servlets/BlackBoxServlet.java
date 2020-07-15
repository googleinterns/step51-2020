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

// runs all the pending DSA campaigns for the logged-in user through the black box and updates estimation results
@WebServlet("/black-box")
public class BlackBoxServlet extends HttpServlet {
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {	
        UserService userService = UserServiceFactory.getUserService();

        if (userService.isUserLoggedIn()) {
            String userId = userService.getCurrentUser().getUserId();

            // get the pending DSA campaigns associated with the logged-in user from datastore
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Query query = new Query("DSACampaign").setFilter(new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId))
                .setFilter(new Query.FilterPredicate("campaignStatus", Query.FilterOperator.EQUAL, "pending"));
            PreparedQuery results = datastore.prepare(query);
            
            for (Entity DSACampaignEntity : results.asIterable()) {
                // run the DSA campaign through the black box
                blackbox(DSACampaignEntity);
            }

            response.sendRedirect("/Home/home.html");
        } else {
            response.sendRedirect("/index.html");
        }
    }

    /*
     * Runs the DSA campaign and obtains estimation results.
     * Updates the DSA campaign entity with the estimation results and changes the campaign status from pending to complete.
     * Implementation explanations are in the design doc.
     */
    public static void blackbox(Entity DSACampaignEntity) {
        // retrieve the DSA campaign's corresponding keyword campaign from datastore
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("keywordCampaign").setFilter(new Query.FilterPredicate("keywordCampaignId", Query.FilterOperator.EQUAL, (String) DSACampaignEntity.getProperty("keywordCampaignId")));
    	Entity keywordCampaignEntity = datastore.prepare(query).asSingleEntity();

        // calculate the estimation results
        double websiteFactor = getWebsiteFactor(keywordCampaignEntity, DSACampaignEntity);
        double impressionsToClicksFactor = getImpressionsToClicksFactor(keywordCampaignEntity, DSACampaignEntity, websiteFactor);
        int impressions = getImpressionsEstimate(keywordCampaignEntity, DSACampaignEntity, websiteFactor);
        int clicks = (int) Math.round(impressions * impressionsToClicksFactor);;
        double cost = clicks * ((double) DSACampaignEntity.getProperty("manualCPC"));

        // if exceeded the daily budget, must cap impressions, clicks, and cost
        if (cost > ((double) DSACampaignEntity.getProperty("dailyBudget"))) {
            cost = (double) DSACampaignEntity.getProperty("dailyBudget");
            clicks = (int) Math.round(cost / ((double) DSACampaignEntity.getProperty("manualCPC")));
            impressions = (int) Math.round(clicks / impressionsToClicksFactor);
        }

        // update the DSA campaign entity in datastore with the estimation results
        DSACampaignEntity.setProperty("impressions", impressions);
        DSACampaignEntity.setProperty("clicks", clicks);
        DSACampaignEntity.setProperty("cost", cost);
        DSACampaignEntity.setProperty("campaignStatus", "complete");
        datastore.put(DSACampaignEntity);

        // TODO: SQR
    }

    public static double getWebsiteFactor(Entity keywordCampaignEntity, Entity DSACampaignEntity) {
        // TODO
        return 1;
    }

     public static int getImpressionsEstimate(Entity keywordCampaignEntity, Entity DSACampaignEntity, double websiteFactor) {
        double manualCPCFactor = getManualCPCFactor(keywordCampaignEntity, DSACampaignEntity);
        double locationsFactor = getLocationsFactor(keywordCampaignEntity, DSACampaignEntity);
        double upliftFactor = .25*manualCPCFactor + .25*locationsFactor + .50*websiteFactor;
        return (int) Math.round(upliftFactor * ((int) keywordCampaignEntity.getProperty("impressions")));
    }

    public static double getManualCPCFactor(Entity keywordCampaignEntity, Entity DSACampaignEntity) {
        double ratio = ((double) DSACampaignEntity.getProperty("manualCPC")) / ((double) keywordCampaignEntity.getProperty("manualCPC"));
        ratio = Math.sqrt(ratio);
        return Math.min(ratio, 3);
    }

    public static double getLocationsFactor(Entity keywordCampaignEntity, Entity DSACampaignEntity) {
        // TODO
        return 1;
    }

    public static double getImpressionsToClicksFactor(Entity keywordCampaignEntity, Entity DSACampaignEntity, double websiteFactor) {
        double adTextFactor = getAdTextFactor(DSACampaignEntity);
        return 1 - (1 / (.80*websiteFactor + .20*adTextFactor));
    }

    public static double getAdTextFactor(Entity DSACampaignEntity) {
        // an ad description line can be at most 90 characters, so 4 lines = 360 characters
        if (((String) DSACampaignEntity.getProperty("adText")).length() > 360) {
            return 1;
        } else {
            return 1.25;
        }
    }
}