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

package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Logs in and logs out the user while also accessing userdata
// retrieved from having logged in. The data is stored in a 
// Json and then sent back as a response.
@WebServlet("/userapi")
public class UserApiServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{

        JsonObject loginInfo = new JsonObject();
        UserService userService = UserServiceFactory.getUserService();

        // Store whether user is logged in.
        boolean isLoggedIn = userService.isUserLoggedIn();
        String url;
        String email;
        String id;

        // If user is logged in send logout is index.html, else home.html is login
        if (isLoggedIn) {
            url = userService.createLogoutURL("../index.html");
            email = userService.getCurrentUser().getEmail();
            id = userService.getCurrentUser().getUserId();
        }
        else {
            url = userService.createLoginURL("../Home/home.html");
            email = "";
            id = "";
        }
        
        // Store info in Json object.
        loginInfo.addProperty("Url", url);
        loginInfo.addProperty("isLoggedIn", isLoggedIn);
        loginInfo.addProperty("Email", email);
        loginInfo.addProperty("id", id);


        // Send info back as response.
        response.setContentType("application/json;");
        response.getWriter().println(loginInfo.toString());
    }
}
