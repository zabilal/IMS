package com.nova.services;

import com.nova.data.MongoDB;
import com.nova.data.models.User;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by olyjosh on 27/11/2017.
 */
public class ServiceEndPoints {

    private Vertx vertx;
    private final String PREFIX = "/api/v1/";
    private MongoDB db;
    private static final String STATUS = "status";
    private static final String MESSAGE = "message";

    public ServiceEndPoints(Vertx vertx, Router router) {
        this.vertx = vertx;

        db = MongoDB.getInstance();
        createPoints(router);
    }

    private void createPoints(Router router) {
//        Router router = Router.router(vertx);
        router.post(PREFIX + "register").handler(this::registerHandler);
    }

    private void registerHandler(RoutingContext rc) {
        String phone = rc.request().getFormAttribute("phone");
//        body.getString("phone")
        User user = new User("", phone, "", "");
        db.addUser(user, stringAsyncResult -> {
            JsonObject res = new JsonObject().put(STATUS, Boolean.TRUE).put(MESSAGE, stringAsyncResult.result());
            rc.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(res.toString());
        });
    }

    private void loginHandler(RoutingContext rc) {

    }


}
