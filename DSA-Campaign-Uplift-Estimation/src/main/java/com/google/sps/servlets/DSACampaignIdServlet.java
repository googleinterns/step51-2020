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

import com.google.sps.classes.DSACampaign;
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

// gets the DSA campaigns from datastore corresponding to the DSA campaign id's in the array from the request
@WebServlet("/DSA-campaign-id")
public class DSACampaignIdServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] DSACampaignIdList = request.getParameter("DSACampaignIds").split(" ");

        ArrayList<DSACampaign> DSACampaignsList = new ArrayList<DSACampaign>();

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        for (String DSACampaignId : DSACampaignIdList) {
            Query query = new Query("DSACampaign").setFilter(new Query.FilterPredicate("DSACampaignId", Query.FilterOperator.EQUAL, DSACampaignId));
            Entity entity = datastore.prepare(query).asSingleEntity();
            
            DSACampaignsList.add(DSACampaignsServlet.createDSACampaignFromEntity(entity));
        }

        Gson gson = new Gson();
        String json = gson.toJson(DSACampaignsList);
        response.setContentType("application/json;");
        response.getWriter().println(json);
    }
}