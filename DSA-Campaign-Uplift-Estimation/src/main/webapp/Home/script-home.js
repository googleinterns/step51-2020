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
let currentPage = 0;

google.charts.load('current', {'packages': ['bar', 'table']});

// Retrieves a list of avaliable keyword campaigns the user has stored and is
// used in dropdown menu.
function getKeywordCampaigns() {
  const keywordCampaignList = document.getElementById('keyword-campaigns');
  keywordCampaignList.innerHTML = '<option value=0>Select a keyword' +
    ' campaign</option>';

  fetch('/keyword-campaigns').then((response) =>
    response.json()).then((keywordCampaigns) => {
    keywordCampaigns.forEach((keywordCampaign) => {
      keywordCampaignList.innerHTML += '<option value=' +
        keywordCampaign.keywordCampaignId + '>' + keywordCampaign.name  +
        '</option>';
    });

    if (keywordCampaigns.length == 0) {
      const dsaCampaignsList = document.getElementById('DSA-campaigns');
      dsaCampaignsList.innerHTML = '<p>There are no keyword campaigns.' +
        ' Please create one.</p>';
    }
  });
}

// Checks if user has selected a keyword campaign. If the user has selected a
// keyword then the for loop draws 2 graphs and creates pagination to explore
// other charts.
function drawDsaCampaignCharts() {
  const numChartsPerPage = 2;
  const dsaCampaignsList = document.getElementById('DSA-campaigns');
  dsaCampaignsList.innerHTML = '<p>First select a keyword campaign.</p>';
  const keywordCampaignId = document.getElementById('keyword-campaigns').value;
  const firstBarChart = document.getElementById('Chart1');
  const secondBarChart = document.getElementById('Chart2');

  if (keywordCampaignId != 0) {
    dsaCampaignsList.innerHTML = '';

    fetch('/DSA-campaigns?keywordCampaignId=' +
      keywordCampaignId).then((response) =>
      response.json()).then((DSACampaigns) => {
      const isNumberOfChartsOdd = (DSACampaigns.length % numChartsPerPage);

      // chartsToShow checks what the active page and give a starting
      // point to display the next two charts.
      let chartsToShow = currentPage * numChartsPerPage;

      // chartCounter keeps track of which number each chart displayed is
      // (useful for drawing the graph) and is used as a flag to check if
      // two charts have been processed.
      let chartCounter = 1;

      // If the list of DSA campaigns are empty than display there are no
      // DSA campaigns and hide any previously showing charts and tables.
      if (DSACampaigns.length == 0) {
        dsaCampaignsList.innerHTML = '<p>There are no DSA campaigns. ' +
          'Please create one.</p>';
        dsaCampaignsList.innerHTML += '<a href=\"../Create/create.html\" ' +
          'style=\"text-decoration: none;\">Here</a>';

        firstBarChart.style.visibility = 'hidden';
        secondBarChart.style.visibility = 'hidden';
      } else {
        while (chartCounter <= numChartsPerPage) {
          if (DSACampaigns[chartsToShow].campaignStatus != 'pending') {
            drawDSACampaignBarGraph(DSACampaigns[chartsToShow], chartCounter);
          } else {
            drawPendingBlock(chartCounter);
          }

          drawDSACampaignTable(DSACampaigns[chartsToShow], chartCounter);
          hideDiv(isNumberOfChartsOdd, DSACampaigns.length, chartsToShow);
          chartsToShow++;
          chartCounter++;
        }
        makePagination((DSACampaigns.length / numChartsPerPage) +
          (DSACampaigns.length % numChartsPerPage), (currentPage));
      }
    });
  } else {
    firstBarChart.style.visibility = 'hidden';
    secondBarChart.style.visibility = 'hidden';
  }
}

// Hides the second chart if the last pair of charts only contains one
// chart left to display.
function hideDiv(isNumberOfChartsOdd, numberOfCharts, currentChart) {
  const firstBarChart = document.getElementById('Chart1');
  const secondBarChart = document.getElementById('Chart2');

  if ( numberOfCharts != 0 ) {
    firstBarChart.style.visibility = 'visible';
  }

  if (isNumberOfChartsOdd == 1 && currentChart == numberOfCharts - 1 ||
    numberOfCharts == 0) {
    secondBarChart.style.visibility = 'hidden';
  } else {
    secondBarChart.style.visibility = 'visible';
  }
}

// Creates pagination html for the user to see other charts by taking the
// existing number of pages and the active page the user is on.
function makePagination(numberOfPages, activePageNumber) {
  const pagination = document.getElementById('pagination');
  let paginationString = '';
  paginationString += '<div class=\"pageCenter\"><input type=\"button\" ' +
    'id=\"previous\" onclick=\"previousPage()\" value=\"previous\" />';
  paginationString += '<a id=\"activePageNumber\">  ' + (activePageNumber +
    1)  +'  </a>';
  paginationString += '<input type=\"button\" id=\"next\" ' +
    'onclick=\"nextPage(' + numberOfPages + ')\" value=\"next\" /></div>';
  pagination.innerHTML = paginationString;
}

// Increases the page forward by increasing the active page by one and letting
// the display charts increase to the next two and rerunning getDSACampaigns to
// retrieve the charts.
function nextPage(numberOfPages) {
  const activePage = document.getElementById('activePageNumber');
  let increasePage = parseInt(activePage.innerText);

  // The if statement checks if the user clicks nextpage but there is
  // no more charts to look at using numberOfPages.
  if (increasePage + 1 <= numberOfPages) {
    increasePage++;
    currentPage++;
    drawDsaCampaignCharts();
  }
  activePage.innerText = increasePage;
}

// Decreases the page backward by decreasing the active page by one and letting
// the display charts decrease to the previous two and rerunning
// getDSACampaigns to retrieve the charts.
function previousPage() {
  const activePage = document.getElementById('activePageNumber');
  let decreasePage = parseInt(activePage.innerText);

  // The if statement checks if the user clicks previousPage but there is
  // no more charts to look at using 0 because there are no negative pages.
  if (decreasePage - 1 > 0) {
    decreasePage--;
    currentPage--;
    drawDsaCampaignCharts();
  }
  activePage.innerText = decreasePage;
}

// This function draws the bar graph that displays the name, impressions,
// clicks, and cost.
function drawDSACampaignBarGraph(DSACampaign, chartNumber) {
  const data = new google.visualization.DataTable();
  data.addColumn('string', 'DSA Campaign');
  data.addColumn('number', 'Impressions');
  data.addColumn('number', 'Clicks');
  data.addColumn('number', 'Cost (USD)');

  data.addRow([DSACampaign.name, DSACampaign.impressions,
    DSACampaign.clicks, DSACampaign.cost]);

  const options = {
    chart: {
      title: DSACampaign.name + ' ' + 'DSA Campaign Metrics',
      subtitle: 'Impressions, Clicks, and Cost',
    },
    bars: 'horizontal', // Required for Material Bar Charts.
  };

  const chart = new google.charts.Bar(document.getElementById('bar-chart' +
    chartNumber));
  chart.draw(data, google.charts.Bar.convertOptions(options));
  console.log('Drew bar graph.');
}

// This function makes three tables for the barchart that was made before this
// function was called. The first table correlates with data and the second
// table correlates with data2. At the end of the function a delete button is
// also created to acommpany the tables.
function drawDSACampaignTable(DSACampaign, chartNumber) {
  // The start of the creation of the first table which includes the name,
  // start date, end date, manual cost per click, and daily budget.
  const data = new google.visualization.DataTable();

  data.addColumn('string', 'DSA Campaign');
  data.addColumn('string', 'Start Date');
  data.addColumn('string', 'End Date');
  data.addColumn('number', 'Manual CPC');
  data.addColumn('number', 'Daily Budget');

  data.addRow([DSACampaign.name, DSACampaign.startDate, DSACampaign.endDate,
    DSACampaign.manualCPC, DSACampaign.dailyBudget]);

  const table = new google.visualization.Table(document.getElementById('table' +
    chartNumber));

  table.draw(data, {showRowNumber: false, width: '100%', height: '50%'});
  // End of the process of first table creation.

  // The start of the creation of the second table which includes the targets,
  // domains, impressions, clicks, cost, and daily budget.
  const data2 = new google.visualization.DataTable();
  data2.addColumn('string', 'Targets');
  data2.addColumn('string', 'Domain');

  // If the DSA campaign is still running then  impressions, clicks, and
  // cost must be N/A.
  if (DSACampaign.campaignStatus == 'pending') {
    data2.addColumn('string', 'Impressions');
    data2.addColumn('string', 'Clicks');
    data2.addColumn('string', 'Cost (USD)');
    data2.addRow([DSACampaign.targets, DSACampaign.domain,
      'N/A', 'N/A', 'N/A']);
  } else {
    data2.addColumn('number', 'Impressions');
    data2.addColumn('number', 'Clicks');
    data2.addColumn('number', 'Cost (USD)');

    data2.addRow([DSACampaign.targets, DSACampaign.domain,
      DSACampaign.impressions, DSACampaign.clicks, DSACampaign.cost]);
  }

  const table2 = new google.visualization.Table(document.getElementById(
      'secondtable' + chartNumber));

  table2.draw(data2, {showRowNumber: false, width: '100%', height: '100%'});
  // End of the process of second table creation.

  // The start of the creation of the second table which includes the location,
  // negative locations, ad text, and campaigns status.
  const data3 = new google.visualization.DataTable();

  data3.addColumn('string', 'Locations');
  data3.addColumn('string', 'Negative Locations');
  data3.addColumn('string', 'Ad Text');
  data3.addColumn('string', 'Status');

  // If the negative location data has ", USA" then there was no negative
  // location set and should be explained in the table chart.
  let negativeLocations = '';
  if (DSACampaign.negativeLocations == ', USA' ) {
    negativeLocations = 'No negative locations.';
  } else {
    negativeLocations = DSACampaign.negativeLocations;
  }

  data3.addRow([DSACampaign.locations, negativeLocations,
    DSACampaign.adText, DSACampaign.campaignStatus]);

  const table3 = new google.visualization.Table(document.getElementById(
      'thirdtable' + chartNumber));
  table3.draw(data3, {allowHtml: true, showRowNumber: false, width: '100%',
    height: '50%'});
  // End of the process of third table creation.

  // This marks the beginning of the delte button process. We define the html
  // of the deletebutton id and link it to the deleteDSACampaign function when
  // clicked.
  const deleteElement = document.getElementById('deletebutton' +
    chartNumber);
  let deleteString = '';
  deleteString += '<button onclick=\"deleteDSACampaign(' +
    DSACampaign.DSACampaignId+')\" class=\"deleteCampaign\"> Delete </button>';
  deleteElement.innerHTML = deleteString;
}

// Sends the id from related DSA campaigns to the DSACampaign servlet where
// it will use the ID to delete the campaign from the container where its held.
function deleteDSACampaign(id) {
  console.log('Start Deleting DSA');

  const params = new URLSearchParams();
  params.append('id', id);
  params.append('delete', true);
  fetch('/DSA-campaigns', {method: 'POST', body: params});

  // Reload page if on first page.
  if (currentPage == 0) {
    location.reload();
  }
  // Reload charts if not on first page.
  currentPage = 0;
  drawDsaCampaignCharts();
}

// Used when a campaign is till running/pending. This function creates
// a block that displays the campaign is still processing and replaces the
// chart that would display normally.
function drawPendingBlock(chartNumber) {
  const pendingBlockElement = document.getElementById('bar-chart' +
    chartNumber);
  let blockString = '';
  blockString += '<div class=\"pendingblock\"><h3>' +
    'Campaign is still processing </h3></div>';
  pendingBlockElement.innerHTML = blockString;
}
