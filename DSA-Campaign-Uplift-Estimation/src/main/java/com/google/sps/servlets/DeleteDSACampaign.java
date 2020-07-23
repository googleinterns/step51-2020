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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/delete-DSACampaign")
public class DeleteDSACampaign extends HttpServlet {
    

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String id = request.getParameter("id");
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query query =
        new Query("DSACampaign")
            .setFilter(new Query.FilterPredicate("DSACampaignId", Query.FilterOperator.EQUAL, id));
    Entity entity = datastore.prepare(query).asSingleEntity();

    long entityId = entity.getKey().getId();

    System.err.println("Deleting this ID:" + entityId);
    Key taskEntityKey = KeyFactory.createKey("DSACampaign", entityId);
    
    datastore.delete(taskEntityKey);
  }
}
