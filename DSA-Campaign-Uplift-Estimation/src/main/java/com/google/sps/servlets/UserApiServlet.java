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

@WebServlet("/userapi")
public class UserApiServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{

        JsonObject loginInfo = new JsonObject();
        UserService userService = UserServiceFactory.getUserService();

        // Store whether user is logged in.
        Boolean isLoggedIn = userService.isUserLoggedIn();
        String url,
            email;

        // If user is logged in send logout is index.html, else home.html is login
        if(isLoggedIn) {
            url = userService.createLogoutURL("../index.html");
            email = userService.getCurrentUser().getEmail();
        }
        else {
            url =userService.createLoginURL("../Home/home.html");
            email = "";
        }
        
        // Store info in Json object.
        loginInfo.addProperty("Url", url);
        loginInfo.addProperty("Bool", isLoggedIn);
        loginInfo.addProperty("Email", email);

        // Send info back as response.
        response.setContentType("application/json;");
        response.getWriter().println(loginInfo.toString());
    }
}