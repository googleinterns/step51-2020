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

package com.google.sps.data;

// contains all the necessary information about a DSA campaign, including id's, input settings, and statistics
public class DSACampaign {
	public int DSACampaignId;
  public int userId;
  public int keywordCampaignId;

  // settings
	public String name;
  public String fromDate;
  public String toDate;
  public double dailyBudget;
  public String location;
  public String domain;
  public String target;

  // estimation results
  public int impressions;
  public int clicks;
  public double cost;

  public DSACampaign(int DSACampaignId, int userId, int keywordCampaignId, String name, String fromDate, String toDate, double dailyBudget, String location, String domain, String target, int impressions, int clicks, double cost) {
    this.DSACampaignId = DSACampaignId;
    this.userId = userId;
    this.keywordCampaignId = keywordCampaignId;
    this.name = name;
    this.fromDate = fromDate;
    this.toDate = toDate;
    this.dailyBudget = dailyBudget;
    this.location = location;
    this.domain = domain;
    this.target = target;
    this.impressions = impressions;
    this.clicks = clicks;
  }

  public DSACampaign(int DSACampaignId, String userId, int keywordCampaignId, String name, String fromDate, String toDate, double dailyBudget, String location, String domain, String target) {
    this.DSACampaignId = DSACampaignId;
    this.tempUserId = userId;
    this.keywordCampaignId = keywordCampaignId;
    this.name = name;
    this.fromDate = fromDate;
    this.toDate = toDate;
    this.dailyBudget = dailyBudget;
    this.location = location;
    this.domain = domain;
    this.target = target;
    this.impressions = 0;
    this.clicks = 0;
    this.cost = 0;
  }
}