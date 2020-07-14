// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the 'License');
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an 'AS IS' BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/* global variables used throughout creation phase. */
let userId = 0;

let keywordCampaignId = null;

// number of locations
let locationCount = 1;

// number of negative locations
let negLocationCount = 1;

// array to keep track of all preset names belonging to user
const USERPRESETS = [];

/* length of mm-dd-yyyy (error handling) */
const DATE_LENGTH = 10;

/**
 * Submission form only requires 2 decimals, this function enforces that rule
 */
function setTwoNumberDecimal() {
  this.value = parseFloat(this.value).toFixed(2);
}

/**
 * verifyLoginStatus() is ran on page load
 * and obtains the user email associated with the user.
 *
 * @returns user login status
 */
function verifyLoginStatus() {
  fetch('/userapi').then(response => response.json()).then(loginStatus => {
    userId = loginStatus.id;
    if (!loginStatus.isLoggedIn) {
      window.location.replace('../index.html');
    }
    // update preset data once login verified.
    updatePresetData(); 
    return loginStatus.isLoggedIn;
  });

  return false;
}

/**
 * This function allows preset data to be saved while the form is being
 * filled out. Alerts the user of the status of their saved preset.
 */
async function submitPresetData() {
  const xmlhttp= window.XMLHttpRequest ?
    new XMLHttpRequest() : new ActiveXObject('Microsoft.XMLHTTP');

  xmlhttp.onreadystatechange = function() {
    if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
      alert('Preset saved!');
      updatePresetData();
    }
    else if ((xmlhttp.status < 200) && (xmlhttp.status >= 400)) {
      alert('Error: Preset cannot be saved. Please try again later.');
    }
  }

  /*
   * error handling for the preset nickname prompt.
   * JS prompt sticks until valid name input.
   */
  let presetName;
  presetLoop:
  while (true) {
    presetName = prompt('What would you like to call the preset?', '');

    // user clicked cancel
    if (presetName == null) {
      return;
    }

    // start error handling.
    if (presetName != '') {
      for (var index = 0; index < USERPRESETS.length; index++) {
        if (USERPRESETS[index].presetId === presetName) {
          alert('Preset name already exists! Please pick a different name.');
          continue presetLoop;
        }
      }
      break;
    }
    else {
      alert('Preset name is not valid! Please pick another name.');
    }
  }

  // dynamically build a URI string with form elements
  var keyvalPairs = [];

  // Encode email, user ID, and preset ID into POST URI string.
  keyvalPairs.push(encodeURIComponent('presetId') + '=' + encodeURIComponent(presetName));
  keyvalPairs.push(encodeURIComponent('userId') + '=' + encodeURIComponent(userId));
  keyvalPairs = addFormElements(keyvalPairs);

  // divide each parameter with '&'
  var queryString = keyvalPairs.join('&');
  
  xmlhttp.open('POST', '/preset', true);
  xmlhttp.setRequestHeader('Content-type','application/x-www-form-urlencoded');
  console.log(queryString);
  xmlhttp.send(queryString);
}

/*
 * updatePresetData sends a GET request to '/preset' with a userId parameter.
 * Upon successful request, the preset data in the comment form is updated
 * with all updated links.
 */
function updatePresetData() {
  if (userId != 0) {
    fetch('/preset?userId=' + userId).then(response => response.json()).then(presetData => {
      console.log(presetData);
      document.getElementById('preset-container').innerHTML = '';
      for (var i = 0; i < presetData.length; i++) {
        console.log('index: ' + i);
        var presetContainer = document.getElementById('preset-container');
        var liElement = document.createElement('li');
        var aTag = document.createElement('a');
        aTag.innerText = presetData[i].presetId;
        aTag.setAttribute('href', 'javascript:;');
        aTag.id = i;
        aTag.setAttribute('onclick', `getPresetData(${i});`);

        // for error handling (cannot create a preset name if it already exists)
        USERPRESETS.push(presetData[i]);

        liElement.appendChild(aTag);
        presetContainer.appendChild(liElement);
      }
    });
  }
}

/**
 * getPresetData() updates the creation form with the specified
 * index. The index correlates to the object location in USERPRESETS.
 * @param indexSelection index of USERPRESETS that user selects.
 */
function getPresetData(indexSelection) {
  var presetSelection = USERPRESETS[indexSelection].campaignData;
  var keywordSelection = presetSelection.keywordCampaignId;
  for (var keywordIndex = 0; keywordIndex < document.getElementById('keyword-campaigns').options.length; keywordIndex++) {
    if (document.getElementById('keyword-campaigns').options[keywordIndex].value === keywordSelection) {
      console.log(keywordSelection);
      var selectedOption = document.getElementById('keyword-campaigns').options[keywordIndex];

      // mark keyword campaign
      selectedOption.selected = true;
      break;
    }
    else {
      var selectedOption = document.getElementById('keyword-campaigns').options[keywordIndex];
      selectedOption.selected = false;
    }
  }
  for (var key in presetSelection) {
    if ((key != 'DSACampaignId') && (key != 'keywordCampaignId') &&
        (key != 'userId') && (key != 'cost') && (key != 'impressions') && 
        (key != 'clicks') && (key != 'locations') && (key != 'campaignStatus')) {
      document.getElementById(key).value = presetSelection[key];
    }
    
    if (key == 'locations') {
      fillOutLocations(presetSelection[key].split(','), false);
    }
    else if (key == 'negativeLocations') {
      fillOutLocations(presetSelection[key].split(','), true);
    }
  }
}

/**
 * fillOutLocations is called upon a preset click. This function appends and
 * sets location nodes based on preset data.
 *
 * @param locationsArray     the array of locations to be set
 * @param isNegativeLocation boolean representing whether the locations are negative locations
 */
function fillOutLocations(locationsArray, isNegativeLocation) {
  let regionIndex = 1;
  console.log(locationsArray);
  for (var locationIndex = 0; locationIndex < locationsArray.length; locationIndex++) {
    let regionId = isNegativeLocation ? `gds-ncr-${regionIndex}` : `gds-cr-${regionIndex}`;
    regionIndex++;
    var currElement = null;
    if (document.getElementById(regionId) != null) {
      currElement = document.getElementById(regionId);
    }
    else {
      addRegion(isNegativeLocation, false);
      currElement = document.getElementById(regionId);
    }

    for (var stateSelection = 0; stateSelection < currElement.options.length; stateSelection++) {
      if (currElement.options[stateSelection].value == locationsArray[locationIndex]) {
        currElement.options[stateSelection].selected = true;
      }
      else {
        currElement.options[stateSelection].selected = false;
      }
    }
  }
}

/**
 * sendFormData is called on submission button click and creates a query
 * string appending the parameter values from form. Once query string is
 * built, it is sent to '/DSA-campaigns' to be processed and analyzed. If
 * error occurs at any point during creation phase, then the function terminates.
 */
function sendFormData() {
  const xmlhttp= window.XMLHttpRequest ?
    new XMLHttpRequest() : new ActiveXObject('Microsoft.XMLHTTP');

  xmlhttp.onreadystatechange = function() {
    if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
      // once form is submitted, redirect to home page.
      window.location.href = '../Home/home.html';
    }
    else if ((xmlhttp.status < 200) && (xmlhttp.status >= 400)) {
      alert('Could not submit form, please try again.')
    }
  }

  // dynamically build a URI string with form elements
  var keyvalPairs = [];

  // verify that user is logged in before submission.
  if (userId == 0) {
    alert('Login before submitting!');
    return;
  }
  keyvalPairs.push(encodeURIComponent('userId') + '=' + encodeURIComponent(userId));

  // verify that keywordCampaignId is set.
  if (keywordCampaignId == null) {
    alert('Select a keyword campaign before submitting!');
    return;
  }

  // default values for variables (not applicable to creation phase) sent to servlet
  keyvalPairs.push(encodeURIComponent('DSACampaignId') + '=' + encodeURIComponent('0'));
  keyvalPairs.push(encodeURIComponent('campaignStatus') + '=' + encodeURIComponent('pending'));

  /* temporary random generation for data */
  let clicks = Math.ceil(Math.random() * 500000);
  let cost = Math.random() * 23000;
  let impressions = Math.ceil(Math.random() * 40000);

  keyvalPairs.push(encodeURIComponent('clicks') + '=' + encodeURIComponent(clicks));
  keyvalPairs.push(encodeURIComponent('cost') + '=' + encodeURIComponent(cost));
  keyvalPairs.push(encodeURIComponent('impressions') + '=' + encodeURIComponent(impressions));
  keyvalPairs = addFormElements(keyvalPairs);

  // separate each entity in keyvalPairs with '&' for query string
  var queryString = keyvalPairs.join('&');

  xmlhttp.open('POST', '/DSA-campaigns', true);
  xmlhttp.setRequestHeader('Content-type','application/x-www-form-urlencoded');
  console.log(queryString);
  xmlhttp.send(queryString);
}

/**
 * addFormElements parses through the campaign form and returns an encoded key value
 * array to be used for POST requests.
 *
 * @param keyvalPairs existing array to contain new form data.
 * @returns           updated keyvalPairs array with campaign form data
 */
function addFormElements(keyvalPairs) {
  keyvalPairs.push(encodeURIComponent('keywordCampaignId') + '=' + encodeURIComponent(keywordCampaignId));
  var form = document.getElementById('campaign-form'); // get the comment form
  let locationString = '';
  let negLocationString = '';
  for (var i = 0; i < form.elements.length; i++) {
    // Form contains buttons that are irrelevant to input - need to filter out only input
    if (form.elements[i].nodeName === 'BUTTON') {
      continue;
    }

    // stop submission process if parameter is required and empty.
    if ((form.elements[i].required) && ((form.elements[i].value === null) || (form.elements[i].value === ''))) {
      console.log(form.elements[i].nodeName);
      console.log(form.elements[i].required);
      alert('Not all the settings are filled out!');
      return;
    } else if ((form.elements[i].name.includes('Date')) &&
               (form.elements[i].value.length > DATE_LENGTH)) {
      alert('Date must be in the format mm/dd/yyyy!');
      return;
    }
    
    // push parameter name and value to keyvalPairs.
    if (!form.elements[i].name.includes('region') && !(form.elements[i].name.includes('country'))) {  
      console.log(`value ${form.elements[i].name} ${form.elements[i].value}`);
      keyvalPairs.push(encodeURIComponent(form.elements[i].name) + '=' + encodeURIComponent(form.elements[i].value));
    }
    else if (form.elements[i].name.includes('nregion')) {
      negLocationString = negLocationString == '' ? form.elements[i].value : negLocationString + ',' + form.elements[i].value;
    }
    else if (form.elements[i].name.includes('region')) {
      locationString = locationString == '' ? form.elements[i].value : locationString + ',' + form.elements[i].value;
    }
  }

  keyvalPairs.push(encodeURIComponent('negativeLocations') + '=' + encodeURIComponent(negLocationString));
  keyvalPairs.push(encodeURIComponent('locations') + '=' + encodeURIComponent(locationString));

  return keyvalPairs;
}

/**
 * processes each onchange event associated with the keyword-campaigns
 * select element. If element value is 0 (no selected element) then form 
 * does not show. If not 0 (selected element), then form shows.
 */
function keywordSelection() {
  var selectElementValue = document.getElementById('keyword-campaigns').value;
  if (selectElementValue == 0) {
    document.getElementById('campaign-form').style.display = 'none';
    document.getElementById('buttons').style.display = 'none';
    keywordCampaignId = null;
  }
  else {
    document.getElementById('campaign-form').style.display = 'block';
    document.getElementById('buttons').style.display = 'inline';
    keywordCampaignId = selectElementValue;
  }
}

/**
 * addRegion is purposed to add location regions to the form
 * where necessary. Terminates if criteria for new region is not met.
 *
 * @param negativeRegion boolean specifying if region is a negative location
 * @param submission     boolean specifying if region is being appended during submission process (for error handling) 
 */
function addRegion(negativeRegion, submission) {
  let regionId = negativeRegion ? 'gds-ncr-' : 'gds-cr-';
  let countryId = negativeRegion ? 'ncountry' : 'country';
  let locationId = negativeRegion ? 'negativeLocations' : 'locations';

  var tempCount = 1;
  let chosenValues = [];
  // verify that all existing locations specified before creating new input (only if submitting)
  while ((tempCount <= locationCount) && submission) {
    var regionSelection = document.getElementById(regionId + '' + tempCount);
    if (regionSelection.options[regionSelection.selectedIndex].value == '') {
      let specifyMsg = negativeRegion ? 'Specify negative region ' + tempCount + ' first!' : 'Specify region ' + tempCount + ' first!';
      alert(specifyMsg);
      return;
    }
    else {
      if (chosenValues.includes(regionSelection.options[regionSelection.selectedIndex].value)) {
        let duplicateMsg = negativeRegion ? 'Please remove duplicate negative regions!' : 'Please remove duplicate regions!';
        alert(duplicateMsg);
        return;
      }
      chosenValues.push(regionSelection.options[regionSelection.selectedIndex].value);
    }
    tempCount++;
  }

  ++locationCount;

  // create the location input HTML elements
  let regionSelect = document.getElementById(`${regionId}${locationCount - 1}`).cloneNode(true);
  regionSelect.id = `${regionId}${locationCount}`;

  let countrySelect = document.getElementById(`${countryId}${locationCount - 1}`).cloneNode(true);
  countrySelect.id = `${countryId}${locationCount}`;
  
  var locations = document.getElementById(locationId);
  
  var locationDiv = document.createElement('div');
  locationDiv.className = 'form-group';
  
  let locationTag = document.createElement('h3');
  let locationTagString = negativeRegion ? `Negative Location ${locationCount}` : `Location ${locationCount}`;
  locationTag.innerText = locationTagString;
  locationDiv.appendChild(locationTag);
  
  var countryLabel = document.createElement('label');
  countryLabel.className = 'control-label';
  countryLabel.innerText = `Country ${locationCount}`;
  
  locationDiv.appendChild(countryLabel);
  locationDiv.appendChild(countrySelect);
  locationDiv.appendChild(document.createElement('br'));


  var regionLabel = document.createElement('label');
  regionLabel.className = 'control-label';
  regionLabel.innerText = `Region ${locationCount}`;
  regionLabel.setAttribute('for', `${regionId}${locationCount}`);

  locationDiv.appendChild(regionLabel);
  locationDiv.appendChild(regionSelect);
  locationDiv.appendChild(document.createElement('br'));

  locations.appendChild(locationDiv);
}
