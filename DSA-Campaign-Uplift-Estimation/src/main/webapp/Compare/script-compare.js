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
    DSACampaignsList.innerHTML = '<p>First select a keyword campaign.</p>';
    var keywordCampaignId = document.getElementById("keyword-campaigns").value;

    if (keywordCampaignId != 0) {
        DSACampaignsList.innerHTML = '';

        fetch('/DSA-campaigns?keywordCampaignId=' + keywordCampaignId).then(response => response.json()).then(DSACampaigns => {
            DSACampaigns.forEach(DSACampaign => {
                DSACampaignsList.innerHTML += '<input class=\"checkbox\" type=\"checkbox\" value=' + DSACampaign.DSACampaignId + '>';
                DSACampaignsList.innerHTML += '<label>' + DSACampaign.name + '</label>';
            });
        });
        DSACampaignsList.innerHTML += '<input id="keyword-campaign-id-form" type=\"hidden\" name=\"keywordCampaignId\" value=' + keywordCampaignId + '>';
        DSACampaignsList.innerHTML += '<input type=\"submit\" value=\"Submit\">';

        console.log('Got DSA campaigns.');
    }
}

function drawBarGraph() {
    var keywordCampaignId = document.getElementById("keyword-campaign-id-form").value;

    // add to DSACampaignIds the id's of all the DSA campaigns whose check boxes were checked
    var DSACampaignIds = "";
    var inputElements = document.getElementsByClassName('checkbox');
    for (var i=0; inputElements[i]; i++) {
        if (inputElements[i].checked) {
            DSACampaignIds += inputElements[i].value + " ";
        }
    }

    fetch('/keyword-campaign-id?keywordCampaignId=' + keywordCampaignId).then(response => response.json()).then(keywordCampaign => {
        fetch('/DSA-campaign-id?DSACampaignIds=' + DSACampaignIds).then(response => response.json()).then(DSACampaignList => {
            var data = new google.visualization.DataTable();
            data.addColumn('string', 'DSA Campaign');
            data.addColumn('number', 'Impressions Uplift');
            data.addColumn('number', 'Clicks Uplift');
            data.addColumn('number', 'Cost Uplift (USD)');

            DSACampaignList.forEach(DSACampaign => {
                data.addRow([DSACampaign.name, calculateUplift(DSACampaign.impressions, keywordCampaign.impressions), calculateUplift(DSACampaign.clicks, keywordCampaign.clicks), 
                            calculateUplift(DSACampaign.cost, keywordCampaign.cost)]);
            });

            var options = {
                chart: {
                    title: 'Statistics',
                    subtitle: 'Impressions, Clicks, and Cost Uplift',
                },
                bars: 'horizontal' // Required for Material Bar Charts.
            };

            var chart = new google.charts.Bar(document.getElementById('bar-chart'));
            chart.draw(data, google.charts.Bar.convertOptions(options));
            console.log("Drew bar graph.");

            drawTable(DSACampaignList, keywordCampaign);
        });
    });
}

function drawTable(DSACampaignList, keywordCampaign) {
    var data = new google.visualization.DataTable();
    data.addColumn('string', 'DSA Campaign');
    data.addColumn('string', 'Campaign Status');
    data.addColumn('string', 'Start Date');
    data.addColumn('string', 'End Date');
    data.addColumn('number', 'Manual CPC');
    data.addColumn('number', 'Daily Budget');
    data.addColumn('string', 'Locations');
    data.addColumn('string', 'Domain');
    data.addColumn('string', 'Targets');
    data.addColumn('string', 'Ad Text');
    data.addColumn('number', 'Impressions Uplift');
    data.addColumn('number', 'Clicks Uplift');
    data.addColumn('number', 'Cost Uplift (USD)');

    DSACampaignList.forEach(DSACampaign => {
        var locations = "";
        for (var i=0; i<DSACampaign.locations.length; i++) {
            locations += DSACampaign.locations[i] + " ";
        }
        var targets = " ";
        for (var i=0; i<DSACampaign.targets.length; i++) {
            targets += DSACampaign.targets[i] + " ";
        }
        data.addRow([DSACampaign.name, DSACampaign.campaignStatus, DSACampaign.startDate, DSACampaign.endDate, DSACampaign.manualCPC, DSACampaign.dailyBudget, 
            locations, DSACampaign.domain, targets, DSACampaign.adText, calculateUplift(DSACampaign.impressions, keywordCampaign.impressions), 
            calculateUplift(DSACampaign.clicks, keywordCampaign.clicks), calculateUplift(DSACampaign.cost, keywordCampaign.cost)]);
    });

    var table = new google.visualization.Table(document.getElementById('table'));
    table.draw(data, {showRowNumber: true, width: '100%', height: '100%'});

    console.log("Drew table.");
}

function calculateUplift(DSAStatistic, keywordCampaignStatistic) {
    var uplift = DSAStatistic - keywordCampaignStatistic;
    // uplift must be non-negative
    if (uplift < 0) {
        return 0;
    }
    return uplift;
}