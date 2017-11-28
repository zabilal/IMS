package com.nova.data;

/**
 * Created by olyjosh on 28/11/2017.
 */
import com.nova.config.K;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
//import io.vertx.examples.utils.Runner;
import io.vertx.ext.mongo.MongoClient;

public class MongoDB {

    private Vertx vertx;
    private MongoClient mongoClient;

    public MongoDB(Vertx vertx) throws Exception {
        this.vertx = vertx;
        start();
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

        mongoClient = MongoClient.createShared(vertx, mongoconfig);

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

}
