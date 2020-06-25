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

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// returns all of the keyword campaigns in datastore
@WebServlet("/keyword-campaigns")
public class EmptyServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("keyword-campaign").addSort("keywordCampaignId", SortDirection.DESCENDING);
    	PreparedQuery results = datastore.prepare(query);

        ArrayList<keywordCampaign> keywordCampaigns = new ArrayList<keywordCampaign>();
        for(Entity entity : results.asIterable()) {
            // TODO
            // keywordCampaign keywordCampaignObject = new keywordCampaign();
            keywordCampaigns.add(keywordCampaignObject);
        }

        Gson gson = new Gson();
        String json = gson.toJson(keywordCampaigns);
        response.setContentType("application/json;");
        response.getWriter().println(json);
    }
    }
}