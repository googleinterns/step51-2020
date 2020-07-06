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

// contains all the necessary information about a DSA campaign preset to be used on create.html
// This class is used to parse preset POST requests and to send preset data upon server request.
public class CampaignPreset {
  String userId; //unique user ID assigned to each user
  String presetId; //unique preset ID assigned to the preset
  DSACampaign campaignData;  //Campaign data (settings) associated with the preset.

  /**
   * Initializes an instance of CampaignPreset 
   *
   * @param userId       the user ID associated with the preset.
   * @param presetId     the preset ID associated with the preset.
   * @param campaignData the DSA campaign settings that the preset contains.
   */
  public CampaignPreset(String userId, String presetId, DSACampaign campaignData) {
    this.userId = userId;
    this.presetId = presetId;
    this.campaignData = campaignData;
  }
}