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

// represents the settings of a keyword campaign that can be copied to a DSA campaign
// contains all the necessary information about a keyword campaign, including id, input settings, and statistics
public class KeywordCampaign {

    public int keywordCampaignId;
    public int userId;
    // the id's of all the DSA campaigns associated with this keyword campaign
    public ArrayList<Integer> DSACampaignIds;

    // settings
    public String name;
    public String fromDate;
    public String toDate;
    public double dailyBudget;
    public String location;
    public String domain;
    public String target;

    // statistics
    public int impressions;
    public int clicks;
    public double cost;

    public KeywordCampaign(int keywordCampaignId, int userId, String name, String fromDate, String toDate, double dailyBudget, String location, String domain, String target, int impressions, int clicks, double cost, ArrayList<Integer> DSACampaignIds) {
        this.keywordCampaignId = keywordCampaignId;
        this.userId = userId;
        this.name = name;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.dailyBudget = dailyBudget;
        this.location = location;
        this.domain = domain;
        this.target = target;
        this.impressions = impressions;
        this.clicks = clicks;
        this.cost = cost;
        this.DSACampaignIds = DSACampaignIds;
    }
}