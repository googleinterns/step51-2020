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

      if (DSACampaigns.length == 0) {
        dsaCampaignsList.innerHTML = '<p>There are no DSA campaigns. ' +
          'Please create one.</p>';
        dsaCampaignsList.innerHTML += '<a href=\"../Create/create.html\" ' +
          'style=\"text-decoration: none;\">Here</a>';
      } else {
        while (chartCounter <= numChartsPerPage) {
          if (DSACampaigns[chartsToShow].campaignStatus != "pending") {
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
  }
}

// Hides the second chart if the last pair of charts only contains one
// chart left to display.
function hideDiv(isNumberOfChartsOdd, numberOfCharts, currentChart) {
  const secondBarChart = document.getElementById('Chart2');

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

function drawDSACampaignTable(DSACampaign, chartNumber) {
  const data = new google.visualization.DataTable();
  
  data.addColumn('string', 'DSA Campaign');
  data.addColumn('string', 'Start Date');
  data.addColumn('string', 'End Date');
  data.addColumn('number', 'Manual CPC');
  data.addColumn('number', 'Daily Budget');
  data.addColumn('string', 'Location');
  data.addColumn('string', 'Domain');

  data.addRow([DSACampaign.name, DSACampaign.startDate, DSACampaign.endDate,
    DSACampaign.manualCPC, DSACampaign.dailyBudget, DSACampaign.locations,
    DSACampaign.domain]);

  const table = new google.visualization.Table(document.getElementById('table' +
    chartNumber));

  table.draw(data, {showRowNumber: false, width: '100%', height: '50%'});

  const data2 = new google.visualization.DataTable();
  data2.addColumn('string', 'Target');
  data2.addColumn('string', 'Ad Text');

  if (DSACampaign.impressions == 0) {
    data2.addColumn('string', 'Impressions');
    data2.addColumn('string', 'Clicks');
    data2.addColumn('string', 'Cost (USD)');
    data2.addRow([DSACampaign.targets, DSACampaign.adText,
        "N/A", "N/A", "N/A"]);
  } else {
      data2.addColumn('number', 'Impressions');
      data2.addColumn('number', 'Clicks');
      data2.addColumn('number', 'Cost (USD)');

      data2.addRow([DSACampaign.targets, DSACampaign.adText,
      DSACampaign.impressions, DSACampaign.clicks, DSACampaign.cost]);
  }
  //data2.addRow([DSACampaign.targets, DSACampaign.adText,
  //  DSACampaign.impressions, DSACampaign.clicks, DSACampaign.cost]);

  const table2 = new google.visualization.Table(document.getElementById(
      'secondtable' + chartNumber));

  table2.draw(data2, {showRowNumber: false, width: '100%', height: '100%'});

  const data3 = new google.visualization.DataTable();

  data3.addColumn('string', 'Status');
  data3.addColumn('string', 'SQR');
  data3.addColumn('string', 'Delete');
  
  let SQR = "";
  if (DSACampaign.impressions == 0) {
    SQR = "N/A";
  } else {
      SQR = "<a href=\"#SQR\" " +
        "style=\"text-align: center;\"> SQR </a>";
  }
  data3.addRow([DSACampaign.campaignStatus, SQR,
  '<button onclick=\"deleteDSACampaign(' + DSACampaign.DSACampaignId +
  ')\" class=\"deleteCampaign\"> Delete </button>']);

  const table3 = new google.visualization.Table(document.getElementById(
      'deletebutton' + chartNumber));
  table3.draw(data3, {allowHtml: true, showRowNumber: false, width: '100%',
    height: '50%'});
}

function deleteDSACampaign(id) {
  console.log('Start Deleting DSA');

  const params = new URLSearchParams();
  params.append('id', id);
  params.append('delete', true);
  fetch('/DSA-campaigns', {method: 'POST', body: params});

  if (currentPage == 0) {
    location.reload();
  }

  currentPage = 0;
  drawDsaCampaignCharts();
}

function drawPendingBlock(chartNumber) {
  const pendingBlockElement = document.getElementById('bar-chart' +
    chartNumber);
    let blockString = '';
    blockString += '<div class=\"pendingblock\"><h3>' +
    'Campaign is still processing </h3></div>';
    pendingBlockElement.innerHTML = blockString;

}