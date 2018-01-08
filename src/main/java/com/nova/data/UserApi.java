package com.nova.data;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

public class UserApi {

    private UserDAO dao;

    private JsonObject response;

    public UserApi(UserDAO dao){
        this.dao = dao;
    }

    public void newUserApi(RoutingContext context){
        JsonObject data = context.getBodyAsJson();
        dao.newUser(data, car->{
            if(car.succeeded()){
                String result = car.result();
                response = new JsonObject()
                .put("status", Boolean.TRUE)
                .put("id", result);
                context.response().end(response.encodePrettily());
            }
            else {
                response = new JsonObject()
                    .put("status", Boolean.FALSE)
                    .put("message", car.cause());
                context.response().end(response.encodePrettily());
            }
        });
    }

    public void checkRegisteredContacts(RoutingContext context){
        JsonObject contactPayload = context.getBodyAsJson();

    }

    public void getAllUserApi(RoutingContext context){
        dao.getAllUsers(car->{
            if(car.succeeded()){
                List<JsonObject> result = car.result();
                context.response().end(result.toString());
            }
        });
    }
}
