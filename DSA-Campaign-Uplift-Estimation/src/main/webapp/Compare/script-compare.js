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

google.charts.load('current', {'packages':['bar']});

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
            var count = 0;
            DSACampaigns.forEach(DSACampaign => {
                if (DSACampaign.campaignStatus == 'complete') {
                    DSACampaignsList.innerHTML += '<input class=\"checkbox\" type=\"checkbox\" value=' + DSACampaign.DSACampaignId + '>';
                    DSACampaignsList.innerHTML += '<label>' + DSACampaign.name + '</label>';
                    count = count + 1;
                }
            });

            if (count == 0) {
                // no complete DSA campaigns associated with the keyword campaign
                DSACampaignsList.innerHTML += '<p>No completed DSA campaigns associated with this keyword campaign.</p>';
            } else {
                DSACampaignsList.innerHTML += '<input id="keyword-campaign-id-form" type=\"hidden\" name=\"keywordCampaignId\" value=' + keywordCampaignId + '>';
                DSACampaignsList.innerHTML += '<input type=\"submit\" value=\"Submit\" style=\"margin-left: 15px;\">';
            }
        });

        console.log('Got DSA campaigns.');
    }
}

function drawBarGraph() {
    // reset the existing graphs
    var chartContainer = document.getElementById('bar-chart');
    chartContainer.innerHTML = '';
    var table = document.getElementById('table');
    table.innerHTML = '';
    var SQR = document.getElementById("SQR");
    SQR.innerHTML = '';

    var keywordCampaignId = document.getElementById("keyword-campaign-id-form").value;

    // add to DSACampaignIds the id's of all the DSA campaigns whose check boxes were checked
    var DSACampaignIds = "";
    var inputElements = document.getElementsByClassName('checkbox');
    var numCheckedElements = 0;
    
    for (var i=0; inputElements[i]; i++) {
        if (inputElements[i].checked) {
            DSACampaignIds += inputElements[i].value + " ";
            numCheckedElements++;
        }
    }

    // make sure at least one DSA campaign was selected
    if (numCheckedElements > 0) {
        fetch('/keyword-campaign-id?keywordCampaignId=' + keywordCampaignId).then(response => response.json()).then(keywordCampaign => {
            fetch('/DSA-campaign-id?DSACampaignIds=' + DSACampaignIds).then(response => response.json()).then(DSACampaignList => {
                var data = new google.visualization.DataTable();
                data.addColumn('string', 'DSA Campaign');
                data.addColumn('number', 'Impressions Uplift');
                data.addColumn('number', 'Clicks Uplift');
                data.addColumn('number', 'Cost Uplift (USD)');

                DSACampaignList.forEach(DSACampaign => {
                    var campaignDuration = getCampaignDuration(DSACampaign.startDate, DSACampaign.endDate);
                    data.addRow([DSACampaign.name, calculateUplift(DSACampaign.impressions, keywordCampaign.impressions, campaignDuration), 
                                calculateUplift(DSACampaign.clicks, keywordCampaign.clicks, campaignDuration), 
                                calculateUplift(DSACampaign.cost, keywordCampaign.cost, campaignDuration)]);
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
}

function drawTable(DSACampaignList, keywordCampaign) {
    var table = document.getElementById('table');

    // create the table
    var settingsTable = document.createElement("TABLE"); 
    settingsTable.style.fontSize = "small";

    // create the row of headers
    var headers = ["DSA Campaign", "Start Date", "End Date", "Manual CPC", "Daily Budget", "Locations", "Negative Locations", "Domain", "Targets",
                      "Ad Text", "Impressions Uplift", "Clicks Uplift", "Cost Uplift (USD)"];
    createRow(settingsTable, "TH", headers);

    // create the rest of the rows
    DSACampaignList.forEach(DSACampaign => {
        var campaignDuration = getCampaignDuration(DSACampaign.startDate, DSACampaign.endDate);

        var rowElements = [DSACampaign.name, DSACampaign.startDate, DSACampaign.endDate, DSACampaign.manualCPC, DSACampaign.dailyBudget, DSACampaign.locations,
                               DSACampaign.negativeLocations, DSACampaign.domain, DSACampaign.targets, DSACampaign.adText, 
                               calculateUplift(DSACampaign.impressions, keywordCampaign.impressions, campaignDuration),
                               calculateUplift(DSACampaign.clicks, keywordCampaign.clicks, campaignDuration),
                               calculateUplift(DSACampaign.cost, keywordCampaign.cost, campaignDuration)];
        createRow(settingsTable, "TD", rowElements);
    });

    settingsTable.style.width = "75%";
    table.appendChild(settingsTable);  

    table.style.paddingTop = "75px";
    table.style.paddingBottom = "75px";

    console.log("Drew table.");

    drawSQRs(DSACampaignList);
}

function drawSQRs(DSACampaignList) {
    var SQRDiv = document.getElementById("SQR");

    DSACampaignList.forEach(DSACampaign => {
        // set up the div for the SQR
        var SQRContainer = document.createElement("div"); 

        var h3 = document.createElement('H3');
        h3.innerHTML = "SQR for " + DSACampaign.name;
        h3.style.textAlign = "center";
        SQRContainer.appendChild(h3);  

        // create the SQR table
        var SQRTable = document.createElement("TABLE"); 
        SQRTable.style.fontSize = "small";

        // create the row of headers
        var headers = ["Query", "URL"];
        createRow(SQRTable, "TH", headers);

        // create the rest of the rows
        DSACampaign.SQR.forEach(SQRrow => {
            var queryUrlElements = [SQRrow[0], SQRrow[1]];
            createRow(SQRTable, "TD", queryUrlElements);
        });

        SQRTable.style.width = "35%";
        SQRContainer.appendChild(SQRTable);

        SQRContainer.style.paddingBottom = "75px";

        SQRDiv.appendChild(SQRContainer);
    });

    console.log("Drew SQRs.");
}

function createRow(container, elementType, textArr) {
    var row = document.createElement("TR");

    textArr.forEach(text => {
        var header = document.createElement(elementType);
        header.appendChild(document.createTextNode(text));
        row.appendChild(header);
    });

    container.appendChild(row);
}

function getCampaignDuration(startDate, endDate) {
    var yearStart = parseInt(startDate.substring(0, 4));
    var monthStart = parseInt(startDate.substring(5, 7));
    var dayStart = parseInt(startDate.substring(8, 10));

    var yearEnd = parseInt(endDate.substring(0, 4));
    var monthEnd = parseInt(endDate.substring(5, 7));
    var dayEnd = parseInt(endDate.substring(8, 10));

    var duration = 360 * (yearEnd - yearStart) + 30 * (monthEnd - monthStart) + (dayEnd - dayStart);
    if (duration <= 0) {
        return 365;
    } else {
        return duration;
    }
}

function calculateUplift(DSAStatistic, keywordCampaignStatistic, campaignDuration) {
    var uplift = (DSAStatistic - keywordCampaignStatistic) * campaignDuration;
    // uplift must be non-negative
    if (uplift < 0) {
        return 0;
    }
    return uplift;
}