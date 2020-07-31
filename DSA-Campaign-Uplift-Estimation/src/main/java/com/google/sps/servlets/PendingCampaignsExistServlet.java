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
import com.google.sps.classes.DSACampaign;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import java.io.IOException;
import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

@WebServlet("/pending-campaigns-exist")
public class PendingCampaignsExistServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("pendingCampaignsExist");
    	Entity result = datastore.prepare(query).asSingleEntity();

        int status = 0;

        if (result != null) {
            status = (int) ((long) result.getProperty("status"));
        }

        Gson gson = new Gson();
        String json = gson.toJson(status);
        response.setContentType("application/json;");
        response.getWriter().println(json);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {	
        changePendingCampaignsExistStatus(0);
    }

    public static void changePendingCampaignsExistStatus(int status) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query query = new Query("pendingCampaignsExist");
        Entity entity = datastore.prepare(query).asSingleEntity();

        if (entity != null) {
            entity.setProperty("status", status);
            datastore.put(entity);
        } else {
            Entity newEntity = new Entity("pendingCampaignsExist");
            newEntity.setProperty("status", status);
            datastore.put(newEntity);
        }
    }
}