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

// global variables used throughout creation phase.
let userId = 0;

let keywordCampaignId = null;

// number of locations
let locationCount = 1;

// number of negative locations
let negLocationCount = 1;

// preset index selected.
let selectedPreset = -1;

// array to keep track of all preset names belonging to user
const USER_PRESETS = [];

// length of mm-dd-yyyy (error handling)
const DATE_LENGTH = 10;

// number of max presets (to prevent unecessary page extensions)
const MAX_PRESETS = 20;

// prototype for specific location elements in html.
const LOCATION_SECTION_ID = 'locations';
const NEG_LOCATION_SECTION_ID = 'negativeLocations';

const LOCATION_ID = 'location';
const NEG_LOCATION_ID = 'nlocation';

const REGION_NAME = 'region';
const NEG_REGION_NAME = 'nregion';

const REGION_ID = 'gds-cr-'
const NEG_REGION_ID = 'gds-ncr-'

/**
 * sets the default values for the endDate and startDate
 * elements in the create page.
 */
function setDate() {
  let today = new Date();
  let startYear = today.getFullYear();
  let endYear = today.getFullYear() + 1;
  let day = (today.getMonth() + 1) < 10 ? '0' + (today.getMonth() + 1) : (today.getMonth() + 1);
  let dateString  = '-' + day + '-' + today.getDate();
  document.getElementById('startDate').value = startYear + dateString
  document.getElementById('endDate').value = endYear + dateString
  console.log(startYear + dateString)
  console.log(endYear + dateString)
}

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
  fetch('/userapi').then(response => response.json())
                   .then(loginStatus => {
    userId = loginStatus.Email;
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

  if (USER_PRESETS.length === MAX_PRESETS) {
    alert('Preset limit reached, please delete presets.');
    return;
  }

  xmlhttp.onreadystatechange = function() {
    if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
      alert('Preset saved!');
      updatePresetData();
    } else if ((xmlhttp.status < 200) && (xmlhttp.status >= 400)) {
      alert('Error: Preset cannot be saved. Please try again later.');
    }
  };

  /*
   * error handling for the preset nickname prompt.
   * JS prompt sticks until valid name input.
   */
  let presetName;
  presetLoop:
  while (true) {
    presetName = prompt('What would you like to call the preset? ' +
                        ' (Max 20 presets)', '');

    // user clicked cancel
    if (presetName == null) {
      return;
    }

    // start error handling.
    if (presetName != '') {
      for (let index = 0; index < USER_PRESETS.length; index++) {
        if (USER_PRESETS[index].presetId
            .toLowerCase() === presetName.toLowerCase()) {
          alert('Preset name already exists! Please pick a different name.');
          continue presetLoop;
        }
      }
      break;
    } else {
      alert('Preset name is not valid! Please pick another name.');
    }
  }

  // dynamically build a URI string with form elements
  let keyvalPairs = [];

  // Encode email, user ID, and preset ID into POST URI string.
  keyvalPairs.push(encodeURIComponent('presetId') + '=' +
                   encodeURIComponent(presetName));
  keyvalPairs.push(encodeURIComponent('userId') + '=' +
                   encodeURIComponent(userId));
  keyvalPairs = addFormElements(keyvalPairs);
  if (keyvalPairs == null) {
    return;
  }

  // divide each parameter with '&'
  const queryString = keyvalPairs.join('&');

  xmlhttp.open('POST', '/preset', true);
  xmlhttp.setRequestHeader('Content-type',
      'application/x-www-form-urlencoded');
  console.log(queryString);

  if (determineValidity) {
    xmlhttp.send(queryString);
  }
}

/*
 * updatePresetData sends a GET request to '/preset' with a userId parameter.
 * Upon successful request, the preset data in the comment form is updated
 * with all updated links.
 */
function updatePresetData() {
  if (userId != 0) {
    const presetURL = '/preset?userId=' + userId;
    fetch(presetURL).then(response => response.json()).then(presetData => {
      console.log(presetData);
      document.getElementById('preset-container').innerHTML = '';
      for (let i = 0; i < presetData.length; i++) {
        console.log('index: ' + i);
        const presetContainer = document.getElementById('preset-container');
        const liElement = document.createElement('li');
        const aTag = document.createElement('a');
        aTag.innerText = presetData[i].presetId;
        aTag.setAttribute('href', 'javascript:;');
        aTag.id = i;
        aTag.setAttribute('onclick', `getPresetData(${i});`);

        // for error handling (cannot create preset name if already exists)
        USER_PRESETS.push(presetData[i]);

        liElement.appendChild(aTag);
        presetContainer.appendChild(liElement);
      }
    });
  }
}

/**
 * getPresetData() updates the creation form with the specified
 * index. The index correlates to the object location in USER_PRESETS.
 *
 * @param indexSelection index of USER_PRESETS that user selects.
 */
function getPresetData(indexSelection) {
  if (!confirm('Are you sure you want to apply this preset?' +
               ' All existing values in the form may reset.')) {
    return;
  }

  const presetSelection = USER_PRESETS[indexSelection].campaignData;
  selectedPreset = indexSelection;
  document.getElementById('preset-delete-btn').style.display = 'inline-block';
  const keywordSelection = presetSelection.keywordCampaignId;

  const keywordCampaignElements = document.getElementById('keyword-campaigns')
      .options;
  for (let i = 0; i < keywordCampaignElements.length; i++) {
    if (keywordCampaignElements[i].value === keywordSelection) {
      console.log(keywordSelection);
      const selectedOption = keywordCampaignElements[i];

      // mark keyword campaign
      selectedOption.selected = true;
      break;
    } else {
      const selectedOption = document.getElementById('keyword-campaigns')
          .options[i];
      selectedOption.selected = false;
    }
  }

  let negLocationExists = false;
  for (const key in presetSelection) {
    if (key == LOCATION_SECTION_ID) {
      let locationsArray = presetSelection[key].split(',');
      locationsArray = locationsArray.filter(function(value) {
        if (value.trim() != '') {
          return value.trim();
        }
      });
      fillOutLocations(locationsArray, false);
    } else if (key == NEG_LOCATION_SECTION_ID) {
      negLocationExists = true;
      console.log(presetSelection[key].split(','));
      let locationsArray = presetSelection[key].split(',');
      locationsArray = locationsArray.filter(function(value) {
        if (value.trim() != 'USA' && value.trim() != '') {
          return value.trim();
        }
      });
      fillOutLocations(locationsArray, true);
    } else if (document.getElementById(key) != null) {
      document.getElementById(key).value = presetSelection[key];
    }
  }

  // clean up negative locations if none are in preset
  if (!negLocationExists) {
    fillOutLocations([], true);
  }
}

function deleteCurrentAppliedPreset() {
  const presetId = USER_PRESETS[selectedPreset].presetId;
  if (confirm(`Are you sure you would like to delete ${presetId}?` +
              ` Existing data in the form will not be deleted.`)) {
    const xmlhttp= window.XMLHttpRequest ?
      new XMLHttpRequest() : new ActiveXObject('Microsoft.XMLHTTP');

    xmlhttp.onreadystatechange = function() {
      if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
        alert('Preset deleted.');
        USER_PRESETS.splice(selectedPreset, 1);
        selectedPreset = -1;
        document.getElementById('preset-delete-btn').style.display = 'none';
        updatePresetData();
      } else if ((xmlhttp.status < 200) && (xmlhttp.status >= 400)) {
        alert('Error: Preset cannot be deleted. Please try again later.');
      }
    };

    // dynamically build a URI string with form elements
    const keyvalPairs = [];

    // Encode user ID and preset ID into POST URI string.
    keyvalPairs.push(encodeURIComponent('delete') + '=' +
                     encodeURIComponent(true));
    keyvalPairs.push(encodeURIComponent('userId') + '=' +
                     encodeURIComponent(userId));
    keyvalPairs.push(encodeURIComponent('presetId') + '=' +
                     encodeURIComponent(presetId));

    // divide each parameter with '&'
    const queryString = keyvalPairs.join('&');

    xmlhttp.open('POST', '/preset', true);
    xmlhttp.setRequestHeader('Content-type',
        'application/x-www-form-urlencoded');
    console.log(queryString);
    xmlhttp.send(queryString);
  }
}

/**
 * fillOutLocations is called upon a preset click. This function appends and
 * sets location nodes based on preset data.
 *
 * @param locationsArray     the array of locations to be set
 * @param isNegativeLocation boolean representing whether the locations are
                             negative locations
 */
function fillOutLocations(locationsArray, isNegativeLocation) {
  for (let locationIndex = 0; locationIndex < locationsArray.length;
    locationIndex++) {
    locationsArray[locationIndex] = locationsArray[locationIndex].trim();
    const regionId = isNegativeLocation ? NEG_REGION_ID + (locationIndex + 1) :
                                          REGION_ID + (locationIndex + 1);
    let currElement = null;
    if (document.getElementById(regionId) != null) {
      currElement = document.getElementById(regionId);
    } else {
      addRegion(isNegativeLocation, false);
      currElement = document.getElementById(regionId);
    }

    for (let stateSelection = 0; stateSelection < currElement.options.length;
      stateSelection++) {
      if (currElement.options[stateSelection].value ==
          locationsArray[locationIndex]) {
        currElement.options[stateSelection].selected = true;
      } else {
        currElement.options[stateSelection].selected = false;
      }
    }
  }

  // clean up extra location inputs
  let cleanUpIndex = locationsArray.length + 1;
  let elementVariable = isNegativeLocation ? NEG_LOCATION_ID + cleanUpIndex :
                                             LOCATION_ID + cleanUpIndex;
  while (document.getElementById(elementVariable) != null) {
    if (cleanUpIndex == 1) {
      // don't delete the first location parameter
      const regionId = isNegativeLocation ? NEG_REGION_ID + cleanUpIndex :
                                            REGION_ID + cleanUpIndex;
      document.getElementById(regionId).selectedIndex = 0;
    } else {
      // delete extra location parameters
      document.getElementById(elementVariable).remove();
    }

    cleanUpIndex++;
    elementVariable = isNegativeLocation ? NEG_LOCATION_ID + cleanUpIndex :
                                           LOCATION_ID + cleanUpIndex;
  }
}

/**
 * sendFormData is called on submission button click and creates a query
 * string appending the parameter values from form. Once query string is
 * built, it is sent to '/DSA-campaigns' to be processed and analyzed. If
 * error occurs at any point during creation phase, then the function
 * terminates.
 */
function sendFormData() {
  const xmlhttp= window.XMLHttpRequest ?
    new XMLHttpRequest() : new ActiveXObject('Microsoft.XMLHTTP');

  xmlhttp.onreadystatechange = function() {
    if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
      // once form is submitted, redirect to home page.
      window.location.href = '../Home/home.html';
    } else if ((xmlhttp.status < 200) && (xmlhttp.status >= 400)) {
      alert('Could not submit form, please try again.');
    }
  };

  // dynamically build a URI string with form elements
  let keyvalPairs = [];

  // verify that user is logged in before submission.
  if (userId == 0) {
    alert('Login before submitting!');
    return;
  }

  // verify that keywordCampaignId is set.
  if (keywordCampaignId == null) {
    alert('Select a keyword campaign before submitting!');
    return;
  }

  keyvalPairs = addFormElements(keyvalPairs);
  if (keyvalPairs == null) {
    return;
  }

  // separate each entity in keyvalPairs with '&' for query string
  const queryString = keyvalPairs.join('&');

  xmlhttp.open('POST', '/DSA-campaigns', true);
  xmlhttp.setRequestHeader('Content-type',
      'application/x-www-form-urlencoded');
  console.log(queryString);
  if (determineValidity()) {
    xmlhttp.send(queryString);
  }
}

/**
 * addFormElements parses through the campaign form and returns an encoded key
 * value array to be used for POST requests.
 *
 * @param keyvalPairs existing array to contain new form data.
 * @returns           updated keyvalPairs array with campaign form data
 */
function addFormElements(keyvalPairs) {
  keyvalPairs.push(encodeURIComponent('keywordCampaignId') + '=' +
                   encodeURIComponent(keywordCampaignId));
  // get the comment form  
  const form = document.getElementById('campaign-form');
  let locationString = '';
  let negLocationString = '';
  let specific_region = true;

  for (let i = 0; i < form.elements.length; i++) {
    /*
     * Form contains buttons that are irrelevant to input - need to filter out
     * only input
     */
    if (form.elements[i].nodeName === 'BUTTON') {
      continue;
    }
    console.log(form.elements[i].nodeName)
    // stop submission process if parameter is required and incorrect.
    if ((form.elements[i].required) && ((form.elements[i].value === null) ||
        (form.elements[i].value === ''))) {
      if (form.elements[i].name.includes('startDate')) {
        alert('Start Date is not valid!');
      } else if (form.elements[i].name.includes('endDate')) {
        alert('End Date is not valid!');
      } else if (form.elements[i].name.includes('Budget')) {
        alert('Daily Budget is not valid!');
      } else if (form.elements[i].name.includes('CPC')) {
        alert('Manual CPC is not valid!');
      } else {
        alert('Invalid configuration, please double check your settings.');
      }
      return null;
    } else if ((form.elements[i].name.includes('Date')) &&
               (form.elements[i].value.length > DATE_LENGTH)) {
      alert('Date must be in the format mm/dd/yyyy!');
      return null;
    }

    // push parameter name and value to keyvalPairs.
    const formRegion = REGION_NAME;
    if (!form.elements[i].name.includes(formRegion)) {
      let value = form.elements[i].value == null ? '' : form.elements[i].value
      keyvalPairs.push(encodeURIComponent(form.elements[i].name) + '=' +
                       encodeURIComponent(value));
    } else if (form.elements[i].name.includes('n' + formRegion)) {
      console.log(negLocationString)
      negLocationString = negLocationString == '' ? form.elements[i].value :
                          negLocationString + ', ' + form.elements[i].value;
    } else if (form.elements[i].name.includes(formRegion) && specific_region) {
      if (form.elements[i].value == 'USA') {
        specific_region = false;
        locationString = 'USA'
        continue;
      }
      locationString = locationString == '' ? form.elements[i].value :
                       locationString + ',' + form.elements[i].value;
    }
  }
  console.log(negLocationString)
  keyvalPairs.push(encodeURIComponent(LOCATION_SECTION_ID) + '=' +
                   encodeURIComponent(locationString));
  keyvalPairs.push(encodeURIComponent(NEG_LOCATION_SECTION_ID) + '=' +
                   encodeURIComponent(negLocationString));
  return keyvalPairs;
}

/**
 * processes each onchange event associated with the keyword-campaigns
 * select element. If element value is 0 (no selected element) then form
 * does not show. If not 0 (selected element), then form shows.
 */
function keywordSelection() {
  const selectElementValue = document.getElementById('keyword-campaigns').value;
  if (selectElementValue == 0) {
    document.getElementById('campaign-form').style.display = 'none';
    document.getElementById('buttons').style.display = 'none';
    keywordCampaignId = null;
  } else {
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
 * @param submission     boolean specifying if region is being appended during
 *                       submission process (for error handling)
 */
function addRegion(negativeRegion, submission) {
  const regionId = negativeRegion ? NEG_REGION_ID : REGION_ID;
  const locationId = negativeRegion ? NEG_LOCATION_SECTION_ID :
      LOCATION_SECTION_ID;
  let locationCounter = negativeRegion ? negLocationCount : locationCount;

  let tempCount = 1;
  const chosenValues = [];
  /*
   * verify that all existing locations specified before creating new input
   * (only if submitting)
   */
  while ((tempCount <= locationCounter) && submission) {
    const regionSelection = document.getElementById(regionId + '' + tempCount);
    if (regionSelection.options[regionSelection.selectedIndex].value == '') {
      const specifyMsg = negativeRegion ? 'Specify negative region ' +
                                          tempCount + ' first!' :
                                          'Specify region ' + tempCount +
                                          ' first!';
      alert(specifyMsg);
      return;
    } else {
      if (chosenValues.includes(regionSelection
          .options[regionSelection.selectedIndex]
          .value)) {
        const duplicateMsg = negativeRegion ?
                             'Please remove duplicate negative' +
                             ' regions!' : 'Please remove duplicate regions!';
        alert(duplicateMsg);
        return;
      }
      chosenValues.push(regionSelection.options[regionSelection.selectedIndex]
          .value);
    }
    tempCount++;
  }

  ++locationCounter;

  if (negativeRegion) {
    // reset global variable for negative location count
    negLocationCount = locationCounter;
  } else {
    // reset global variable for regular location count
    locationCount = locationCounter;
  }

  // create the location input HTML elements
  // clone first element (always available)
  const first_element_idx = 1;
  const regionSelect = document.getElementById(
      regionId + first_element_idx).cloneNode(true);
  regionSelect.id = regionId + locationCounter;

  regionSelect.setAttribute('onchange', 'regionSelection()');

  const locations = document.getElementById(locationId);

  const locationDiv = document.createElement('div');
  locationDiv.className = 'form-group';
  locationDiv.id = negativeRegion ? NEG_LOCATION_ID + locationCounter :
                                    LOCATION_ID + locationCounter;

  const countryLabel = document.createElement('label');
  countryLabel.className = 'control-label';
  countryLabel.innerText = negativeRegion ?
                           `Negative Country ${locationCounter}` :
                           `Country ${locationCounter}`;

  const regionLabel = document.createElement('label');
  regionLabel.className = 'control-label';
  regionLabel.innerText = negativeRegion ?
                          `Negative Region ${locationCounter}` :
                          `Region ${locationCounter}`;
  regionLabel.setAttribute('for', regionId + locationCounter);

  locationDiv.appendChild(regionLabel);
  locationDiv.appendChild(regionSelect);
  locationDiv.appendChild(document.createElement('br'));

  location_section = negativeRegion ? 'new_neg_locations' : 'new_locations';
  location_section = document.getElementById(location_section)
  location_section.appendChild(locationDiv);
}

/**
 * determineValidity() uses the built in Javascript
 * checkValidity function to determine if function
 * is ready to submit.
 *
 * @returns boolean representing validity of form.
 */
function determineValidity() {
  const elements = document.getElementsByTagName('input');
  for (let i = 0; i < elements.length; i++) {
    if (!elements[i].checkValidity()) {
      console.log(elements[i].id);
      return false;
    }
  }
  return true;
}

/**
 * used by reset button on campaign form to reset the entries.
 */
function resetCampaignForm() {
  const confirmation = confirm('Are you sure you want to clear the form?');
  if (confirmation) {
    let tempLocationCount = 2;

    // remove extra locations
    while (true) {
      let locationFound = false;
      if (document.getElementById(LOCATION_ID + tempLocationCount) != null) {
        document.getElementById(LOCATION_ID + tempLocationCount).remove();
        locationFound = true;
      }

      if (document.getElementById(NEG_LOCATION_ID + tempLocationCount) != null) {
        document.getElementById(NEG_LOCATION_ID + tempLocationCount).remove();
        locationFound = true;
      }

      if (!locationFound) {
        break;
      }
      tempLocationCount++;
    }
    document.getElementById('campaign-form').reset();
    locationCount = 1;
    negLocationCount = 1;
  }
}

function regionSelection() {
    let usaRegion = false;
    for (let i = 1; i <= locationCount; i++) {
      // USA index = 1
      if (document.getElementById(`gds-cr-${i}`).selectedIndex == 1) {
        usaRegion = true;
      }
    }

    if (!usaRegion) {
      document.getElementById('negativeLocations').style.display = 'none'
      document.getElementById('add_region').style.display = 'inline-block'
    } else {
      document.getElementById('negativeLocations').style.display = 'block'
      document.getElementById('add_region').style.display = 'none'
    }
}
