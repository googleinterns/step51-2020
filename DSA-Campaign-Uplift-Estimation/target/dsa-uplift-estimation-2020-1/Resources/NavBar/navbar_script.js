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

/* Set the width of the side navigation to 250px and the left margin of the page content to 250px and add a black background color to body */
function openNav() {
  document.getElementById("mySidenav").style.width = "250px";
  document.getElementById("main").style.marginLeft = "250px";
  document.body.style.backgroundColor = "rgba(0,0,0,0.4)";
}

/* Set the width of the side navigation to 0 and the left margin of the page content to 0, and the background color of body to white */
function closeNav() {
  document.getElementById("mySidenav").style.width = "0";
  document.getElementById("main").style.marginLeft = "0";
  document.body.style.backgroundColor = "white";
}

/* Checks user's login status and sets logout link accordingly */
function getLogsStatus() {

    // Fetches json from userapi servlet and uses to set the login
    // and logout links for the front end.
    fetch('/userapi').then(response => response.json()).then(logStatus => {
        const link = document.getElementById("loginout-link");

        // If the user is logged in and is at the login screen then the
        // login link should skip login and head into home. If the user is
        // not on the login screen then the url will be to sign out.
        if(logStatus.isLoggedIn) {
            if(window.location.pathname === "/index.html"){
                console.log('User is Logged In');
                link.href = '../Home/home.html';
            }
            else{
                link.href = logStatus.Url;
            }

        }
        // If the user is not logged in and is at the login screen then the
        // login link should require them to login before heading into home.
        // If the user is somehow not on the login screen and isn't logged in
        // then the url will point to the login sceen.
        else {
            if(window.location.pathname === "/index.html"){
                console.log('User is not Logged In');
                link.href = logStatus.Url;
            }
            else{
                link.href = '../index.html';
            }
        }
    });
}