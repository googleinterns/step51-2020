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
import com.google.sps.classes.KeywordCampaign;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.io.IOException;
import java.io.PrintWriter;
import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

// runs all the pending DSA campaigns for the logged-in user through the black box and updates estimation results
@WebServlet("/black-box")
public class BlackBoxServlet extends HttpServlet {
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {	
        UserService userService = UserServiceFactory.getUserService();

        if (userService.isUserLoggedIn()) {
            String userId = userService.getCurrentUser().getUserId();

            // get the pending DSA campaigns associated with the logged-in user from datastore
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Query query = new Query("DSACampaign").setFilter(new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId))
                .setFilter(new Query.FilterPredicate("campaignStatus", Query.FilterOperator.EQUAL, "pending"));
            PreparedQuery results = datastore.prepare(query);
            
            for (Entity DSACampaignEntity : results.asIterable()) {
                // run the DSA campaign through the black box
                blackbox(DSACampaignEntity);
            }

            response.sendRedirect("/Home/home.html");
        } else {
            response.sendRedirect("/index.html");
        }
    }
}