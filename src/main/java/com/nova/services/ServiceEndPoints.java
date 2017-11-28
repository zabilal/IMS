package com.nova.services;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by olyjosh on 27/11/2017.
 */
public class ServiceEndPoints {

    private Vertx vertx;
    private final String PREFIX = "/api/v1/";

    public ServiceEndPoints(Vertx vertx) {
        this.vertx = vertx;
        createPoints();
    }

    private void createPoints(){
        Router router = Router.router(vertx);
        router.get(PREFIX+"register").handler(this::registerHandler);
    }

    private void registerHandler(RoutingContext rc) {
//        rc.response()
//            .putHeader("content-type", "application/json; charset=utf-8")
//            .end(Json.encodePrettily(products.values()));
    }

    private void loginHandler(RoutingContext rc) {

    }



}
