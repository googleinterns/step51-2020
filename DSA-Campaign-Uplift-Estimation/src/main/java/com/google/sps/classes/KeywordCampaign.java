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

// contains all the necessary information about a keyword campaign
public class KeywordCampaign {

    // unique identifier for the keyword campaign
    public String keywordCampaignId;
    // id of the user who created the keyword campaign (user email)
    public String userId;

    // campaign settings

    // name of the keyword campaign (used for display purposes)
    public String name;
    // how much an advertiser spends each time a user clicks on their ad link
    public double manualCPC;
    /*
     * comma-separated string containing the different locations the advertiser wants to target
     * ads will only be targeted towards users living in the specified locations
     * locations must be cities or states in the US, or the US itself
     * locations cannot be abbreviations
     * Ex: correct - "California, Houston, United States of America"; incorrect - "CA, Houston, USA")
     */
    public String locations;
    /*
     * comma-separated string (same format as locations) will contain the locations the advertiser specifically doesn’t want to target
     * Ex: advertiser can choose to target all of the US except for Washington DC
     */
    public String negativeLocations;

    // statistics used in the black box and to calculate DSA campaign uplift

    //  how many times an ad appears on a search result page on average in a single day
    public int impressions;
    // number of clicks on an ad link on average in a single day
    public int clicks;
    // total money spent on the campaign on average in a single day
    public double cost;

    public KeywordCampaign(String keywordCampaignId, String userId, String name, double manualCPC, String locations, String negativeLocations, int impressions, int clicks, double cost) {
        this.keywordCampaignId = keywordCampaignId;
        this.userId = userId;

        this.name = name;
        this.manualCPC = manualCPC;
        this.locations = locations;
        this.negativeLocations = negativeLocations;

        this.impressions = impressions;
        this.clicks = clicks;
        this.cost = cost;
    }
}