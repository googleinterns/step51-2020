// Copyright 2020 Google LLC
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

// Sets the page to always display the first two elements when loading the page.
var secondChart = 2;
var firstChart = 0;

google.charts.load('current', {'packages':['bar', 'table']});

// Retrieves a list of avaliable keyword campaigns the user has stored and is used in
// dropdown menu.
function getKeywordCampaigns() {
	const keywordCampaignList = document.getElementById('keyword-campaigns');
    keywordCampaignList.innerHTML = '<option value=0>Select a keyword campaign</option>';

    fetch('/keyword-campaigns').then(response => response.json()).then(keywordCampaigns => {
        keywordCampaigns.forEach(keywordCampaign => {
            keywordCampaignList.innerHTML += '<option value=' + keywordCampaign.keywordCampaignId + '>' + keywordCampaign.name  + '</option>';
        });
    });
}

// Checks if user has selected a keyword campaign. If the user has selected a keyword
// then the for loop draws 2 graphs and creates pagination to explore other charts.
function getDSACampaigns() {
    const DSACampaignsList = document.getElementById('DSA-campaigns');
    DSACampaignsList.innerHTML = '<p>First select a keyword campaign.</p>';
    var keywordCampaignId = document.getElementById("keyword-campaigns").value;

    if (keywordCampaignId != 0) {
        DSACampaignsList.innerHTML = '';

        fetch('/DSA-campaigns?keywordCampaignId=' + keywordCampaignId).then(response => response.json()).then(DSACampaigns => {

            for(var i = firstChart; i < secondChart; i++ ){
                drawNewBarGraph(DSACampaigns[i], secondChart-i);
            }
            if (firstChart == 0){
                makePagination((DSACampaigns.length / 2) + (DSACampaigns.length % 2), (Math.log(firstChart + 1)/Math.log(2)));
            }
            else{
                makePagination((DSACampaigns.length / 2) + (DSACampaigns.length % 2), (Math.log(firstChart)/Math.log(2)));
            }            
        });
    }
}

// Creates pagination html for the user to see other charts by taking the existing number of pages
// and the active page the user is on.
function makePagination(numberOfPages,activeNumber) {
    const pagination = document.getElementById("pagination");
    var paginationString = "";
    paginationString += "<input type=\"button\" id=\"previous\" onclick=\"previousPage()\" value=\"previous\" />";
    paginationString += "<a id=\"activeNumber\">  " + (activeNumber + 1)  +"  </a>"; 
    paginationString += "<input type=\"button\" id=\"next\" onclick=\"nextPage(" + numberOfPages + ")\" value=\"next\" />";
    pagination.innerHTML = paginationString;
}

// Increases the page forward by increasing the active page by one and letting the display
// charts increase to the next two and rerunning getDSACampaigns to retrieve the charts.
function nextPage(numberOfPages) {
    const activePage = document.getElementById("activeNumber");
    var increasePage = parseInt(activePage.innerText);
    
    // The if statement checks if the user clicks nextpage but there is
    // no more charts to look at using numberOfPages.
    if(increasePage + 1 <= numberOfPages){
        increasePage++;  
        secondChart += 2;
        firstChart += 2;  
    }
    activePage.innerText = increasePage;
    getDSACampaigns();
}

// Decreases the page backward by decreasing the active page by one and letting the display
// charts decrease to the previous two and rerunning getDSACampaigns to retrieve the charts.
function previousPage() {
    const activePage = document.getElementById("activeNumber");
    var decreasePage = parseInt(activePage.innerText);

    // The if statement checks if the user clicks previousPage but there is
    // no more charts to look at using 0 because there are no negative pages.
    if(decreasePage - 1 > 0){
        decreasePage--;  
        secondChart -= 2;
        firstChart -= 2;  
    }
    activePage.innerText = decreasePage;
    getDSACampaigns();
}

function drawNewBarGraph(DSACampaign, chartNumber) {

    var data = new google.visualization.DataTable();
    data.addColumn('string', 'DSA Campaign');
    data.addColumn('number', 'Impressions');
    data.addColumn('number', 'Clicks');
    data.addColumn('number', 'Cost (USD)');

    data.addRow([DSACampaign.name, DSACampaign.impressions, DSACampaign.clicks, DSACampaign.cost]);

    var options = {
        chart: {
            title: 'Statistics',
            subtitle: 'Impressions, Clicks, and Cost',
        },
        bars: 'horizontal' // Required for Material Bar Charts.
    };

    var chart = new google.charts.Bar(document.getElementById('bar-chart' + chartNumber));
    chart.draw(data, google.charts.Bar.convertOptions(options));
    console.log("Drew bar graph.");

    drawNewTable(DSACampaign, chartNumber);
}

function drawNewTable(DSACampaign, chartNumber) {

    var data = new google.visualization.DataTable();
    data.addColumn('string', 'DSA Campaign');
    data.addColumn('string', 'Start Date');
    data.addColumn('string', 'End Date');
    data.addColumn('number', 'Daily Budget');
    data.addColumn('string', 'Location');
    data.addColumn('string', 'Domain');
    data.addColumn('string', 'Target');
    data.addColumn('number', 'Impressions');
    data.addColumn('number', 'Clicks');
    data.addColumn('number', 'Cost (USD)');

    
    data.addRow([DSACampaign.name, DSACampaign.fromDate, DSACampaign.toDate, DSACampaign.dailyBudget, DSACampaign.location,
    DSACampaign.domain, DSACampaign.target, DSACampaign.impressions, DSACampaign.clicks, DSACampaign.cost]);
    

    var table = new google.visualization.Table(document.getElementById('table' + chartNumber));
    table.draw(data, {showRowNumber: true, width: '100%', height: '100%'});

    console.log("Drew table.");
}