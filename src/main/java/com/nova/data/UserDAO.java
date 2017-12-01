package com.nova.data;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.List;

public class UserDAO {

    private MongoClient mongo;

    public static final String TABLE_USERS = "users";


    public UserDAO(MongoClient mongo) {
        this.mongo = mongo;
    }

    public void newUser(JsonObject data, Handler<AsyncResult<String>> handler) {
        mongo.insert(TABLE_USERS, data, handler);
    }

    public void getAllUsers(Handler<AsyncResult<List<JsonObject>>> handler) {
        JsonObject query = new JsonObject();
        mongo.find(TABLE_USERS, query, handler);
    }
}
