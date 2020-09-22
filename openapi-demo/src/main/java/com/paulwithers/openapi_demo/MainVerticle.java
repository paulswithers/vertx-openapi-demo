/*
 * ========================================================================== *
 * Copyright (C) 2020 Paul Withers *
 * All rights reserved. *
 * ========================================================================== *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may *
 * not use this file except in compliance with the License. You may obtain a *
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>. *
 * *
 * Unless required by applicable law or agreed to in writing, software *
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT *
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the *
 * License for the specific language governing permissions and limitations *
 * under the License. *
 * ==========================================================================
 */
package com.paulwithers.openapi_demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.RouterFactory;
import io.vertx.ext.web.openapi.RouterFactoryOptions;
import io.vertx.ext.web.validation.RequestParameters;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    System.out.println("Starting...");

    RouterFactory.create(vertx, "demo.api.json", ar -> {
      if (ar.succeeded()) {
        final RouterFactory routerFactory = ar.result();
        RouterFactoryOptions options = new RouterFactoryOptions();
        routerFactory.setOptions(options);
        
        routerFactory.operation("getEndpointWithOptionals")
          .handler(routingContext -> {
          final RequestParameters params = routingContext.get("parsedParameters");
          JsonObject responseObj = new JsonObject();
          try {
            // This fails
            responseObj = params.toJson();
          } catch (Exception e) {
            // This goes into the response
            responseObj.put("Error", "Something went wrong");
          }
          routingContext.response().putHeader("Content-Type", "application/json").end(responseObj.encode());
        }).failureHandler(routingContext -> {
          System.err.println("Something went wrong!");
        });;
        
        Router router = routerFactory.createRouter();
        
        router.errorHandler(500, this::doGenericError);
        router.errorHandler(404, this::doNotFoundError);
        HttpServer server = vertx.createHttpServer(new HttpServerOptions().setPort(8888).setHost("localhost"));
        server.requestHandler(router).listen();
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        System.err.println(ar.cause());
        startPromise.fail(ar.cause());
      }
    });
  }
  
  private void doGenericError(final RoutingContext ctx) {
    final HttpServerResponse response = ctx.response();
    response.setStatusCode(500).end("Something went wrong");
  }
  
  private void doNotFoundError(final RoutingContext ctx) {
    final HttpServerResponse response = ctx.response();
    response.setStatusCode(404).end("Something went wrong");
  }
}
