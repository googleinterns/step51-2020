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

google.charts.load('current', {'packages':['bar', 'table']});

function getKeywordCampaigns() {
	const keywordCampaignList = document.getElementById('keyword-campaigns');
    keywordCampaignList.innerHTML = '<option value=0>Select a keyword campaign</option>';

    fetch('/keyword-campaigns').then(response => response.json()).then(keywordCampaigns => {
        keywordCampaigns.forEach(keywordCampaign => {
            keywordCampaignList.innerHTML += '<option value=' + keywordCampaign.keywordCampaignId + '>' + keywordCampaign.name  + '</option>';
        });
    });
    console.log('Got keyword campaigns.');
}

function getDSACampaigns() {
    const DSACampaignsList = document.getElementById('DSA-campaigns');
    DSACampaignsList.innerHTML = '<option value=0>Select a DSA campaign</option>';
    var keywordCampaignId = document.getElementById("keyword-campaigns").value;

    if (keywordCampaignId != 0) {
        fetch('/DSA-campaigns?keywordCampaignId=' + keywordCampaignId).then(response => response.json()).then(DSACampaigns => {
            DSACampaigns.forEach(DSACampaign => {
                DSACampaignsList.innerHTML += '<option value=' + DSACampaign.DSACampaignId + '>' + DSACampaign.name + '</option>';
            });
        });
        console.log('Got DSA campaigns.');
    }
}

function drawBarGraph() {
    var DSACampaignId = document.getElementById("DSA-campaigns").value;

    if (DSACampaignId != 0) {
        fetch('/DSA-campaign-id?DSACampaignId=' + DSACampaignId).then(response => response.json()).then(DSACampaign => {
            fetch('/keyword-campaign-id?keywordCampaignId=' + DSACampaign.keywordCampaignId).then(response => response.json()).then(keywordCampaign => {
                var data = google.visualization.arrayToDataTable([
                    ['Statistic', keywordCampaign.name, DSACampaign.name],
                    ['Impressions', keywordCampaign.impressions, DSACampaign.impressions],
                    ['Clicks', keywordCampaign.clicks, DSACampaign.clicks],
                    ['Cost (USD)', keywordCampaign.cost, DSACampaign.cost]
                ]);

                var options = {
                    chart: {
                        title: 'Statistics',
                        subtitle: 'Impressions, Clicks, and Cost (USD)',
                    },
                    bars: 'horizontal' // Required for Material Bar Charts.
                };

                var chart = new google.charts.Bar(document.getElementById('bar-chart'));

                chart.draw(data, google.charts.Bar.convertOptions(options));

                drawTable(DSACampaign, keywordCampaign);
            });
        });

        console.log("Drew bar graph.");
    }
}

function drawTable(DSACampaign, keywordCampaign) {
    var data = google.visualization.arrayToDataTable([
        ['', 'name', 'start date', 'end date', 'daily budget', 'location', 'domain', 'target', 'impressions', 'clicks', 'cost (USD)'],
        ['Keyword Campaign', keywordCampaign.name, keywordCampaign.fromDate, keywordCampaign.toDate, keywordCampaign.dailyBudget, keywordCampaign.location, keywordCampaign.domain, keywordCampaign.target, keywordCampaign.impressions, keywordCampaign.clicks, keywordCampaign.cost],
        ['DSA Campaign', DSACampaign.name, DSACampaign.fromDate, DSACampaign.toDate, DSACampaign.dailyBudget, DSACampaign.location, DSACampaign.domain, DSACampaign.target, DSACampaign.impressions, DSACampaign.clicks, DSACampaign.cost],
    ]);

    var table = new google.visualization.Table(document.getElementById('table'));

    table.draw(data, {showRowNumber: true, width: '100%', height: '100%'});

    console.log("Drew table.");
}