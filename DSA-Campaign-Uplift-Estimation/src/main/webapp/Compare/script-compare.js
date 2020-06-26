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
	fetch('/keyword-campaigns').then(response => response.json()).then(keywordCampaigns => {
        const keywordCampaignList = document.getElementById('keyword-campaigns');
	
        keywordCampaigns.forEach(keywordCampaign => {
            keywordCampaignList.innerHTML += '<a onclick=\"getDSACampaigns(' + keywordCampaign.keywordCampaignId + ')\">Keyword Campaign ' + keywordCampaign.keywordCampaignId + '</a>';
        });
    });
    console.log('Got keyword campaigns.');
}

function getDSACampaigns(keywordCampaignId) {
    fetch('DSA-campaigns?keywordCampaignId=' + keywordCampaignId).then(response => response.json()).then(DSACampaigns => {
        const DSACampaignsList = document.getElementById('DSA-campaigns');

        DSACampaigns.forEach(DSACampaign => {
            DSACampaignsList.innerHTML += '<a href=\"#\">' + DSACampaign.name + '</a>';
        });
    });
}