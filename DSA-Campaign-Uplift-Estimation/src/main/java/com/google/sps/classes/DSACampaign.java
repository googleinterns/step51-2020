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

package com.google.sps.classes;

// contains all the necessary information about a DSA campaign
public class DSACampaign {

    // unique identifier for the DSA campaign
	public String DSACampaignId;
    // id of the user who created the DSA campaign
    public String userId;
    // id of the keyword campaign this DSA campaign was run on
    public String keywordCampaignId;

    // settings

    // name of the DSA campaign (used for display purposes)
	public String name;
    // status of the blackbox data retrieval: "pending" (stats still pending) or "complete" (stats retrieved)
    public String campaignStatus;
    // start date of the DSA campaign: MM/DD/YYYY
    public String startDate;
    // end date of the DSA campaign: MM/DD/YYYY
    public String endDate;
    // how much an advertiser spends each time a user clicks on their ad link
    public double manualCPC;
    // amount an advertiser is willing to spend each day
    public double dailyBudget;
    /*
     * comma-separated string containing the different locations the advertiser wants to target
     * ads will only be targeted towards users living in the specified locations
     * locations must be cities or states in the US, or the US itself
     * locations cannot be abbreviations
     * Ex: correct - "California, Houston, United States of America"; incorrect - "CA, Houston, USA")
     */
    public String locations;
    // url of the main web page to be advertised
    public String domain;
    /*
     * comma-separated string of url's of additional sub pages to be advertised
     * Ex: "url1.com, url2.com"
     */
    public String targets;
    // custom description to display along with the ad
    public String adText;

    // estimation results

    //  how many times an ad appears on a search result page
    public int impressions;
    // how many times a user clicks on an ad link
    public int clicks;
    // total money spent on the ad campaign
    public double cost;

    public DSACampaign(String DSACampaignId, String userId, String keywordCampaignId, String name, String campaignStatus, String startDate, String endDate, double manualCPC, double dailyBudget, String locations, String domain, String targets, String adText, int impressions, int clicks, double cost) {
        this.DSACampaignId = DSACampaignId;
        this.userId = userId;
        this.keywordCampaignId = keywordCampaignId;

        this.name = name;
        this.campaignStatus = campaignStatus;
        this.startDate = startDate;
        this.endDate = endDate;
        this.manualCPC = manualCPC;
        this.dailyBudget = dailyBudget;
        this.locations = locations;
        this.domain = domain;
        this.targets = targets;
        this.adText = adText;

        this.impressions = impressions;
        this.clicks = clicks;
        this.cost = cost;
    }
}