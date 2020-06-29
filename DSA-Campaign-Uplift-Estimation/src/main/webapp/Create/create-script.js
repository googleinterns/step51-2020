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

/* Submission form only requires 2 decimals, this function enforces that rule */
function setTwoNumberDecimal(event) {
  this.value = parseFloat(this.value).toFixed(2);
}


function submitPresetData() {
  let xmlhttp= window.XMLHttpRequest ?
    new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP");

  xmlhttp.onreadystatechange = function() {
    if (xmlhttp.status === 0) {
      console.log(xmlhttp.status);
      alert("Preset saved!");
    }
    else if ((xmlhttp.status < 200) && (xmlhttp.status >= 400)) {
      alert("Error: Preset cannot be saved. Please try again later.");
    }
  }

  // dynamically build a URL string with form elements
  var keyvalpairs = [];
  var form = document.getElementById('campaign-form'); // get the form somehow
  for (var i = 0; i < form.elements.length; i++) {
    var curr_element = form.elements[i];
    keyvalpairs.push(encodeURIComponent(curr_element.name) + "=" + encodeURIComponent(curr_element.value));
  }

  // divide each parameter with '&'
  var queryString = keyvalpairs.join("&");
  
  xmlhttp.open("POST", '/preset', true);
  xmlhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
  xmlhttp.send(queryString);
}