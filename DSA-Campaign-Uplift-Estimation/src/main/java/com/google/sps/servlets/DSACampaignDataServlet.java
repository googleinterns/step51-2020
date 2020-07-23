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
import com.google.sps.WebCrawler;
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
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

// obtains estimation results for all the pending DSA campaigns
@WebServlet("/data")
public class DSACampaignDataServlet extends HttpServlet {
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {	
        // get all the pending DSA campaigns from datastore
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("DSACampaign").setFilter(new Query.FilterPredicate("campaignStatus", Query.FilterOperator.EQUAL, "pending"));
        PreparedQuery results = datastore.prepare(query);
        
        for (Entity DSACampaignEntity : results.asIterable()) {
            // obtain the DSA campaign estimation results
            estimationResults(DSACampaignEntity);
        }

        response.sendRedirect("/Home/home.html");
    }

    /*
     * Runs the DSA campaign and obtains estimation results.
     * Updates the DSA campaign entity with the estimation results and changes the campaign status from pending to complete.
     * Implementation explanations are in the design doc.
     */
    public static void estimationResults(Entity DSACampaignEntity) throws IOException {
        // retrieve the DSA campaign's corresponding keyword campaign from datastore
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("keywordCampaign").setFilter(new Query.FilterPredicate("keywordCampaignId", Query.FilterOperator.EQUAL, (String) DSACampaignEntity.getProperty("keywordCampaignId")));
    	Entity keywordCampaignEntity = datastore.prepare(query).asSingleEntity();

        // calculate the estimation results
        double websiteFactor = WebCrawler.getWebsiteFactor(keywordCampaignEntity, DSACampaignEntity);
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

    public static int getImpressionsEstimate(Entity keywordCampaignEntity, Entity DSACampaignEntity, double websiteFactor) throws IOException {
        double manualCPCFactor = getManualCPCFactor(keywordCampaignEntity, DSACampaignEntity);
        double locationsFactor = getLocationsFactor(keywordCampaignEntity, DSACampaignEntity);
        double upliftFactor = .25*manualCPCFactor + .25*locationsFactor + .50*websiteFactor;

        return (int) Math.round(upliftFactor * ((int) ((long) keywordCampaignEntity.getProperty("impressions"))));
    }

    public static double getManualCPCFactor(Entity keywordCampaignEntity, Entity DSACampaignEntity) {
        double ratio = ((double) DSACampaignEntity.getProperty("manualCPC")) / ((double) keywordCampaignEntity.getProperty("manualCPC"));
        ratio = Math.sqrt(ratio);
        return Math.min(ratio, 3);
    }

    public static double getLocationsFactor(Entity keywordCampaignEntity, Entity DSACampaignEntity) throws IOException {
        // read in US Census data
        BufferedReader file = new BufferedReader(new FileReader("US_Census_Data_State_Populations.txt"));

        // populate the census data into the hash map
        HashMap<String, Long> statePopulations = new HashMap<String, Long>();

        // ignore the first line containing column headers
        file.readLine();
        for (int i=0; i<56; i++) {
            String[] lineElements = file.readLine().split(",");
            String location = lineElements[4].toLowerCase();
            long population = Long.parseLong(lineElements[16]);
            statePopulations.put(location, population);
        }
        file.close();

        double targetPopulationSizeKeywordCampaign = getTargetPopulationSize((String) keywordCampaignEntity.getProperty("locations"), (String) keywordCampaignEntity.getProperty("negativeLocations"), statePopulations);
        double targetPopulationSizeDSACampaign = getTargetPopulationSize((String) DSACampaignEntity.getProperty("locations"), (String) DSACampaignEntity.getProperty("negativeLocations"), statePopulations);

        return Math.sqrt(targetPopulationSizeDSACampaign / targetPopulationSizeKeywordCampaign);
    }

    // Returns the amount of people living in the specified locations, subtracting the amount of people living in the negative locations.
    public static double getTargetPopulationSize(String locations, String negativeLocations, HashMap<String, Long> statePopulations) {
        String refinedLocations = locations.trim().toLowerCase();
        String refinedNegativeLocations = negativeLocations.trim().toLowerCase();

        // ensure that we don't subtract for negative locations if our initial target isn't the entire US
        if (!refinedLocations.equals("united states")) {
            refinedNegativeLocations = "";
        }

        /*
         * In the front end, we ensure that
         * 1) All of the locations are either US states or the US itself.
         * 2) There is no overlap between locations and negative locations.
         * 3) If the US is a location, no other states are given as locations as well.
         * 4) At least one location is given.
         */
        double targetPopulationSize = 0;
        String[] locationsArr = refinedLocations.split(",");
        for (String location : locationsArr) {
            targetPopulationSize += statePopulations.get(location.trim());
        }

        if (!refinedNegativeLocations.equals("")) {
            String[] negativeLocationsArr = refinedNegativeLocations.split(",");
            for (String negativeLocation : negativeLocationsArr) {
                targetPopulationSize -= statePopulations.get(negativeLocation.trim());
            }
        }

        return targetPopulationSize;
    }

    public static double getImpressionsToClicksFactor(Entity keywordCampaignEntity, Entity DSACampaignEntity, double websiteFactor) {
        double adTextFactor = getAdTextFactor(DSACampaignEntity);
        return (1 - (1 / (.80*websiteFactor + .20*adTextFactor))) / 2;
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