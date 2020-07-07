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

import java.util.ArrayList;

// contains all the necessary information about a keyword campaign
public class KeywordCampaign {

    // unique identifier for the keyword campaign
    public String keywordCampaignId;
    // id of the user who created the keyword campaign
    public String userId;
    // the id's of all the DSA campaigns associated with this keyword campaign
    public String[] DSACampaignIds;
    // name of the keyword campaign (used for display purposes)
    public String name;

    // statistics used to calculate the uplift of a DSA campaign (e.g. impressions uplift = DSA campaign impressions - keyword campaign impressions)

    //  how many times an ad appears on a search result page
    public int impressions;
    // how many times a user clicks on an ad link
    public int clicks;
    // total money spent on the ad campaign
    public double cost;

    public KeywordCampaign(String keywordCampaignId, String userId, String[] DSACampaignIds, String name, int impressions, int clicks, double cost) {
        this.keywordCampaignId = keywordCampaignId;
        this.userId = userId;
        this.DSACampaignIds = DSACampaignIds;
        this.name = name;

        this.impressions = impressions;
        this.clicks = clicks;
        this.cost = cost;
    }
}