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

function getKeywordCampaigns() {
	const keywordCampaignList = document.getElementById('keyword-campaigns');
    keywordCampaignList.innerHTML = '<option value=0>Select a keyword campaign</option>';

    fetch('/keyword-campaigns').then(response => response.json()).then(keywordCampaigns => {
        keywordCampaigns.forEach(keywordCampaign => {
            keywordCampaignList.innerHTML += '<option value=' + keywordCampaign.keywordCampaignId + '>Keyword Campaign ' + keywordCampaign.keywordCampaignId  + '</option>';
        });
    });
    console.log('Got keyword campaigns.');
}

function getDSACampaigns() {
    const DSACampaignsList = document.getElementById('DSA-campaigns');
    DSACampaignsList.innerHTML = '<option value=0>Select a DSA campaign</option>';
    var keywordCampaignId = document.getElementById("keyword-campaigns").value;

    fetch('/DSA-campaigns?keywordCampaignId=' + keywordCampaignId).then(response => response.json()).then(DSACampaigns => {
        DSACampaigns.forEach(DSACampaign => {
            DSACampaignsList.innerHTML += '<option value=' + DSACampaign.DSACampaignId + '>' + DSACampaign.name + '</option>';
        });
    });
    console.log('Got DSA campaigns.');
}