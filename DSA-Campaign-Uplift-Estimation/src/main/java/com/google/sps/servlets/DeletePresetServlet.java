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

import com.google.sps.classes.*;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.*;
import java.io.IOException;
import java.io.PrintWriter;
import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.*;
import java.util.ArrayList;

// This servlet is responsible for deleting campaign preset data.
@WebServlet("/delete-preset")
public class DeletePresetServlet extends HttpServlet {

  /**
   * doPost handles POST requests to '/delete-preset'. In this context, it updates
   * preset data with the boolean value of 'true' under 'deleted' in datastore.
   *
   * @param request  (HttpServletRequest) request being made containing the request headers and data
   * @param response (HttpServletResponse) variable used to send back a response to request. 
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String userId = request.getParameter("userId");
    String presetId = request.getParameter("presetId");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Filter userIdFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);
    Query query = new Query("PresetData").setFilter(userIdFilter);
    PreparedQuery results = datastore.prepare(query);
    for (Entity presetEntity : results.asIterable()) {
      if (presetEntity.getProperty("presetId").equals(presetId)) {
        datastore.delete(presetEntity.getKey());
        return;
      }
    }
  }
}
