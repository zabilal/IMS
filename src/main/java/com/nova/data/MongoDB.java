package com.nova.data;

/**
 * Created by olyjosh on 28/11/2017.
 */

import com.nova.config.K;
import com.nova.data.models.Room;
import com.nova.data.models.User;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
//import io.vertx.examples.utils.Runner;
import io.vertx.ext.mongo.MongoClient;

public class MongoDB {

    private Vertx vertx;
    private static MongoClient mongoClient;
    private static MongoDB inst;

    public MongoDB(Vertx vertx) throws Exception {
        this.vertx = vertx;
        start();
        inst = this;
    }

    private void start() throws Exception {

        JsonObject config = Vertx.currentContext().config();
        String uri = config.getString("mongo_uri");
        String db = config.getString("mongo_db");

        if (uri == null) {
            uri = K.MMONGO_URI;
        }

        if (db == null) {
            db = K.MMONGO_DB;
        }

        JsonObject mongoconfig = new JsonObject()
            .put("connection_string", uri)
            .put("db_name", db);

        if (MongoDB.mongoClient == null)
            MongoDB.mongoClient = MongoClient.createShared(vertx, mongoconfig);

//        JsonObject product1 = new JsonObject().put("itemId", "12345").put("name", "Cooler").put("price", "100.0");
//
//        mongoClient.save("products", product1, id -> {
//            System.out.println("Inserted id: " + id.result());
//
//            mongoClient.find("products", new JsonObject().put("itemId", "12345"), res -> {
//                System.out.println("Name is " + res.result().get(0).getString("name"));
//
//                mongoClient.remove("products", new JsonObject().put("itemId", "12345"), rs -> {
//                    if (rs.succeeded()) {
//                        System.out.println("Product removed ");
//                    }
//                });
//
//            });
//
//        });

    }


    public void getUserById(String id, Handler<AsyncResult<User>> aHandler) {
        mongoClient.findOne(getCollName(User.class), new JsonObject().put("_id", id), null, rs -> {
            if (rs.succeeded()) {
                User user = Json.decodeValue(rs.result().toBuffer(), User.class);
                aHandler.handle((AsyncResult<User>) rs.result());
            }
        });
    }

    public void getUserByPhone(String x, Handler<AsyncResult<User>> aHandler) {
        mongoClient.findOne(getCollName(User.class), new JsonObject().put("phone", x), null, rs -> {
            if (rs.succeeded()) {
                User user = Json.decodeValue(rs.result().toBuffer(), User.class);
                aHandler.handle((AsyncResult<User>) rs.result());
            }
        });
    }

    public void getUserByUsername(String x, Handler<AsyncResult<User>> aHandler) {
        mongoClient.findOne(getCollName(User.class), new JsonObject().put("username", x), null, rs -> {
            if (rs.succeeded()) {
                User user = Json.decodeValue(rs.result().toBuffer(), User.class);
                aHandler.handle((AsyncResult<User>) rs.result());
            }
        });
    }

    public void addUser(User x, Handler<AsyncResult<String>> aHandler) {
        String encode = Json.encode(x);
        JsonObject jo = new JsonObject(encode);
        mongoClient.save(getCollName(User.class), jo, rs -> {

            if (rs.succeeded()) {

                aHandler.handle(rs);
            }
        });
    }

    public void addRoom(Room x, Handler<AsyncResult<String>> aHandler) {
        String encode = Json.encode(x);
        JsonObject jo = new JsonObject(encode);
        mongoClient.save(getCollName(User.class), jo, rs -> {
            if (rs.succeeded()) {
                aHandler.handle(rs);
            }
        });
    }


    public void getRoomById(String x, Handler<AsyncResult<Room>> aHandler) {
        mongoClient.findOne(getCollName(Room.class), new JsonObject().put("_id", x), null, rs -> {
            if (rs.succeeded()) {
//                Room Room = Json.decodeValue(rs.result().toBuffer(), Room.class);
                aHandler.handle((AsyncResult<Room>) rs.result());
            }
        });
    }

    public void getRoomByServerId(String x, Handler<AsyncResult<Room>> aHandler) {
        mongoClient.findOne(getCollName(Room.class), new JsonObject().put("serverId", x), null, rs -> {
            if (rs.succeeded()) {
//                Room Room = Json.decodeValue(rs.result().toBuffer(), Room.class);
                aHandler.handle((AsyncResult<Room>) rs.result());
            }
        });
    }


    public void getRoomByServerByOwer(String ownerId, Handler<AsyncResult<Room>> aHandler) {
//        mongoClient.find(getCollName(Room.class), new JsonObject().put("owner", ownerId), rs -> {
//            if (rs.succeeded()) {
////                Room Room = Json.decodeValue(rs.result().toBuffer(), Room.class);
//                aHandler.handle((AsyncResult<Room>) rs.result());
//            }
//        });
    }


    private String getCollName(Class x) {
        return x.getSimpleName() + "s";
    }

    public static MongoDB getInstance() {
        return inst;
    }


}
