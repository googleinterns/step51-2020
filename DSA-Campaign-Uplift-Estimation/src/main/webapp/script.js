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

// Checks user's login status and sets login link accordingly.
function getLogsStatus() {

    fetch('/userapi').then(response => response.json()).then(logStatus => {
        const link = document.getElementById("login-link");

        if(logStatus.isLoggedIn) {

            console.log('User is Logged In');
            link.href = '../Home/home.html';
        }
        else {
            console.log('User is not Logged In');
            link.href = logStatus.Url;
        }
    });
}