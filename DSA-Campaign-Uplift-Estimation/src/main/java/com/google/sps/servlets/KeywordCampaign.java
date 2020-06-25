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

public class KeywordCampaign {

    public int keywordCampaignId;
    public int userId;
    public int impressions;
    public int clicks;
    public int cost;
    public ArrayList<Integer> DSACampaignIds;

    public KeywordCampaign(int keywordCampaignId, int userId, int impressions, int clicks, int cost, ArrayList<Integer> DSACampaignIds) {
        this.keywordCampaignId = keywordCampaignId;
        this.userId = userId;
        this.impressions = impressions;
        this.clicks = clicks;
        this.DSACampaignIds = DSACampaignIds;
    }
}