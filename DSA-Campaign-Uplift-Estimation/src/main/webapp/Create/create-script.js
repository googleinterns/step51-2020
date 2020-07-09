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

/* global variables used throughout creation phase. */

// specifies user email
let userEmail = null;

//specifies user ID
let userId = 0;

//specifies selected keyword campaign ID.
let keywordCampaignId = null;

// number of locations being inputted
let countryCount = 1;

/* 
 * Submission form only requires 2 decimals, this function enforces that rule 
 */
function setTwoNumberDecimal() {
  this.value = parseFloat(this.value).toFixed(2);
}

/*
 * verifyLoginStatus() is ran on page load
 * and obtains the user email associated with the user.
 */
function verifyLoginStatus() {
  fetch('/userapi').then(response => response.json()).then(loginStatus => {
    userEmail = loginStatus.Email;
    userId = loginStatus.id;
    if (!loginStatus.isLoggedIn) {
      window.location.replace("../index.html");
    }
    return loginStatus.isLoggedIn;
  });

  return false;
}

/* 
 * This function allows preset data to be saved while the form is being
 * filled out. Alerts the user of the status of their saved preset. 
 */
function submitPresetData() {
  let xmlhttp= window.XMLHttpRequest ?
    new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP");

  xmlhttp.onreadystatechange = function() {
    if (xmlhttp.status === 0) {
      alert("Preset saved!");
    }
    else if ((xmlhttp.status < 200) && (xmlhttp.status >= 400)) {
      alert("Error: Preset cannot be saved. Please try again later.");
    }
  }

  // error handling for the preset nickname prompt. JS prompt that sticks until valid name is inputted.
  var presetName;
  while (true) {
    presetName = prompt("What would you like to call the preset?", "");
    
    // user clicked cancel
    if (presetName == null) {
      return;
    }

    // start error handling.
    if (presetName != "") {
      //TODO verify that the preset nickname does not already exist.
      break;
    }
    else {
      alert("Preset name is not valid or already exists! Please pick another name.");
    }
  }

  // dynamically build a URI string with form elements
  var keyval_pairs = [];

  // Encode email, user ID, and preset ID into POST URI string.
  keyval_pairs.push(encodeURIComponent("userEmail") + "=" + encodeURIComponent(userEmail));
  keyval_pairs.push(encodeURIComponent("userId") + "=" + encodeURIComponent(userId));
  keyval_pairs.push(encodeURIComponent("presetId") + "=" + encodeURIComponent(presetName));
  
  var form = document.getElementById('campaign-form'); // get the comment form
  for (var i = 0; i < form.elements.length; i++) {
    // stop preset process if parameter is empty.
    if ((form.elements[i].value === null) || (form.elements[i].value === "")) {
      alert("Not all the settings are filled out!");
      return;
    }
    var curr_element = form.elements[i];
    keyval_pairs.push(encodeURIComponent(curr_element.name) + "=" + encodeURIComponent(curr_element.value));
  }

  // divide each parameter with '&'
  var queryString = keyval_pairs.join("&");
  
  xmlhttp.open("POST", '/preset', true);
  xmlhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
  alert(queryString);
  xmlhttp.send(queryString);
}

/*
 * updatePresetData takes in a presetId and userId and sends a GET request
 * to '/preset'. Once received, the preset data in the comment form is updated
 * with all updated links.
 */
function updatePresetData() {
  //TODO
}

/**
 * sendFormData is called on submission button click and creates a query
 * string appending the parameter values from form. Once query string is
 * built, it is sent to '/DSA-campaigns' to be processed and analyzed. If
 * error occurs at any point during creation phase, then the function terminates.
 */
function sendFormData() {
  let xmlhttp= window.XMLHttpRequest ?
    new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP");

  xmlhttp.onreadystatechange = function() {
    if (xmlhttp.status === 0) {
      // once form is submitted, redirect to home page.
      window.location.replace("../Home/home.html");
    }
    else if ((xmlhttp.status < 200) && (xmlhttp.status >= 400)) {
      alert("Could not submit form, please try again.")
    }
  }

  // dynamically build a URI string with form elements
  var keyval_pairs = [];

  // verify that user is logged in before submission.
  if (userId == 0) {
    alert("Login before submitting!");
    return;
  }
  keyval_pairs.push(encodeURIComponent("userId") + "=" + encodeURIComponent(userId));

  //verify that keywordCampaignId is set.
  if (keywordCampaignId == null) {
    alert("Select a keyword campaign before submitting!");
    return;
  }
  keyval_pairs.push(encodeURIComponent("keywordCampaignId") + "=" + encodeURIComponent(keywordCampaignId));
  
  //default values for variables (not applicable to creation phase) sent to servlet
  keyval_pairs.push(encodeURIComponent("DSACampaignId") + "=" + encodeURIComponent("0"));
  keyval_pairs.push(encodeURIComponent("campaignStatus") + "=" + encodeURIComponent("pending"));
  keyval_pairs.push(encodeURIComponent("clicks") + "=" + encodeURIComponent("0"));
  keyval_pairs.push(encodeURIComponent("costs") + "=" + encodeURIComponent("0"));
  keyval_pairs.push(encodeURIComponent("impressions") + "=" + encodeURIComponent("0"));
  
  let location = "";
  var form = document.getElementById('campaign-form'); // get the comment form
  for (var i = 0; i < form.elements.length; i++) {
    // stop preset process if parameter is empty.
    console.log(form.elements[i].nodeName);

    //Form contains buttons that are irrelevant to input - need to filter out only input
    if (form.elements[i].nodeNode != "INPUT" || form.elements[i].nodeName != "SELECT") {
      continue;
    }
    if ((form.elements[i].value === null) || (form.elements[i].value === "")) {
      console.log(form.elements[i].id);
      alert("Not all the settings are filled out!");
      return;
    }
    else if (form.elements[i].name.includes("region")) {
      location = form.elements[i].value + "," + location;
      keyval_pairs.push(encodeURIComponent("locations") + "=" + encodeURIComponent(location));
    }
    else if (form.elements[i].name.includes("country")) {
      location = form.elements[i].value;
    }
    else {
      var curr_element = form.elements[i];
      keyval_pairs.push(encodeURIComponent(curr_element.name) + "=" + encodeURIComponent(curr_element.value));
    }
  }

  // divide each parameter with '&'
  var queryString = keyval_pairs.join("&");
  
  xmlhttp.open("POST", '/DSA-campaigns', true);
  xmlhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
  console.log(queryString);
  xmlhttp.send(queryString);
}

/**
 * processes each onchange event associated with the keyword-campaigns
 * select element. If element value is 0 (no selected element) then form 
 * does not show. If not 0 (selected element), then form shows.
 */
function keywordSelection() {
  var select_element_value = document.getElementById("keyword-campaigns").value;
  if (select_element_value == 0) {
    document.getElementById("campaign-form").style.display = "none";
    document.getElementById("buttons").style.display = "none";
    keywordCampaignId = null;
  }
  else {
    document.getElementById("campaign-form").style.display = "block";
    document.getElementById("buttons").style.display = "inline";
    keywordCampaignId = select_element_value;
  }
}

//TODO Fix additional dropdown menu not showing elements.
function addRegion() {
  var tempCount = 1;
  while (tempCount <= countryCount) {
    if (document.getElementById("country" + tempCount).value == "") {
      alert("Specify country " + tempCount + " first!");
      return;
    }
    else if (document.getElementById("gds-cr-" + tempCount).value == "") {
      alert("Specify region " + tempCount + " first!");
      return;
    }
    tempCount++;
  }
  countryCount++;
  var countryDiv = document.createElement('div');
  countryDiv.className = 'form-group';
  countryDiv.innerHTML = "<label class=\"control-label\">Country " + countryCount + "</label>" +
                         "<select class=\"form-control gds-cr\" country-data-region-id=\"gds-cr-" + countryCount + "\" id=\"country" + countryCount + "\" data-language=\"en\" name=\"country" + countryCount + "\" required></select>";
  var regionDiv = document.createElement('div');
  regionDiv.className = 'form-group';
  regionDiv.innerHTML = "<label for=\"gds-cr-" + countryCount + "\" class=\"control-label\">Region " + countryCount + "</label>" +
                        "<select class=\"form-control\" id=\"gds-cr-" + countryCount + "\" name=\"region" + countryCount + "\" required></select>";
  var locations = document.getElementById("locations");
  locations.appendChild(countryDiv);
  locations.appendChild(regionDiv);
}

// TODO: validate DSA campaign inputs (e.g. campaign status must be "pending" or "complete")