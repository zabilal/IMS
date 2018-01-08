package com.nova.simplechat.simplechat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nova.data.MongoDB;
import com.nova.services.RestServer;
import com.nova.services.ServiceEndPoints;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.eventbus.bridge.tcp.TcpEventBusBridge;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import java.io.IOException;
import java.text.DateFormat;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Raji Zakariyya on 05/01/2018
 *
 * MainVerticle deploys other verticles
 *
 */
public class MainVerticle extends AbstractVerticle {

    public static final int MONGO_PORT = 27017;

    @Override
    public void start(Future<Void> future) throws Exception {

//        startHttpServer();

//        Future<String> start = deployRestServer().setHandler(car->{
//            if(car.succeeded()){
//                vertx.deployVerticle(new ChatVerticle());
//            }else{
//                car.cause();
//            }
//        });

        vertx.deployVerticle(new ChatVerticle());
        vertx.deployVerticle(new EventVerticle());
//        vertx.deployVerticle(new ServerLogger());
    }

    private Future<String> deployRestServer(){

        Future<String> future = Future.future();

        JsonObject dbConfig = new JsonObject();
        dbConfig.put("mongo", mongoConfig());
        DeploymentOptions brokerOptions = new DeploymentOptions();
        brokerOptions.setConfig(dbConfig);

        vertx.deployVerticle(RestServer.class.getName(), brokerOptions, handler ->{
            if (handler.failed()){
                future.fail(handler.cause());
            }else{
                future.complete();
            }
        });

        return future;
    }

    private void v1(){
        final Pattern chatUrlPattern = Pattern.compile("/");
        final EventBus eventBus = vertx.eventBus();
        final Logger logger = LoggerFactory.getLogger(MainVerticle.class);
        final ArrayList<String> users = new ArrayList<>();

        vertx.createHttpServer().websocketHandler(ws -> {
            final Matcher m = chatUrlPattern.matcher(ws.path());
            if (!m.matches()) {
                ws.reject();
                return;
            }
            final String chatRoom = m.group(0);

//            MultiMap headers = ws.headers();
//            final String id = headers.get("id");

            final String id = ws.textHandlerID();
            logger.info("Registering new connection with id: " + id + " for chat-room: " + chatRoom);
            vertx.sharedData().getLocalMap("chat.room." + chatRoom).put(id, id);
            users.add(id);

            ws.handler(data -> {

                int i = 0;
                ObjectMapper m1 = new ObjectMapper();
                try {
                    JsonNode rootNode = m1.readTree(data.toString());
//                    ((ObjectNode) rootNode).put("received", new Date().toString());
                    ((ObjectNode) rootNode).put("received", System.currentTimeMillis());
                    String jsonOutput = m1.writeValueAsString(rootNode);
                    logger.info("json generated: " + jsonOutput);

                    LocalMap<String, Object> localMap = vertx.sharedData().getLocalMap("chat.room." + chatRoom);

                    i++;

                    for (Map.Entry value : localMap.entrySet()) {

                        String address = value.getValue().toString();
                        eventBus.send(address, jsonOutput + i);

                    }

                } catch (IOException e) {
                    ws.reject();
                }
            });


            ws.closeHandler(event -> {
                logger.info("un-registering connection with id: " + id + " from chat-room: " + chatRoom);
                vertx.sharedData().getLocalMap("chat.room." + chatRoom).remove(id);
            });

        }).listen(8080);
    }

    private void v2(){
        Router router = Router.router(vertx);

        // Allow events for the designated addresses in/out of the event bus bridge
        BridgeOptions opts = new BridgeOptions()
          .addInboundPermitted(new PermittedOptions().setAddress("chat.to.server"))
          .addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client"));

        // Create the event bus bridge and add it to the router.
        TcpEventBusBridge bridge = TcpEventBusBridge.create(vertx);
        SockJSHandler ebHandler = SockJSHandler.create(vertx).bridge(opts);
        router.route("/eventbus/*").handler(ebHandler);

        // Create a router endpoint for the static content.
        router.route().handler(StaticHandler.create());

        // Start the web server and tell it to use the router to handle requests.
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);

        EventBus eb = vertx.eventBus();

        //Here we go
//        eb.consumer("chat.to.server", new ChatMessageHandler());

        // Register to listen for messages coming IN to the server
        eb.consumer("chat.to.server").handler(message -> {
          // Create a timestamp string
          String timestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(Date.from(Instant.now()));
          // Send the message back out to all clients with the timestamp prepended.
          eb.publish("chat.to.client", timestamp + ": " + message.body());
        });
    }

    private void startHttpServer() {
        try {
            new MongoDB(vertx);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Router router = Router.router(vertx);
        new ServiceEndPoints(vertx, router);
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    private static JsonObject mongoConfig() {
        JsonObject config = new JsonObject();
        config.put("host", "localhost");
        config.put("port", MONGO_PORT);
        config.put("db_name", "wiseapp");
        return config;
    }

}

