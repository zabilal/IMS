package com.nova.simplechat.simplechat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nova.data.MongoDB;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Raji Zakariyya
 *
 * MainVerticle deploys other verticles
 *
 */
public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {

        v1();
//        vertx.deployVerticle(new ChatVerticle());

        new MongoDB(vertx);

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

            MultiMap headers = ws.headers();

            final String chatRoom = m.group(0);
            final String id = ws.textHandlerID();
//            logger.info("registering new connection with id: " + id + " for chat-room: " + chatRoom);
//            vertx.sharedData().getLocalMap("chat.room." + chatRoom).put(id, id);
//            users.add(id);

            ws.closeHandler(event -> {
                logger.info("un-registering connection with id: " + id + " from chat-room: " + chatRoom);
                vertx.sharedData().getLocalMap("chat.room." + chatRoom).remove(id);
            });

            ws.handler(data -> {

                ObjectMapper m1 = new ObjectMapper();
                try {
                    JsonNode rootNode = m1.readTree(data.toString());
                    ((ObjectNode) rootNode).put("received", new Date().toString());
                    String jsonOutput = m1.writeValueAsString(rootNode);
//                    logger.info("json generated: " + jsonOutput);

                    LocalMap<String, Object> localMap = vertx.sharedData().getLocalMap("chat.room." + chatRoom);
                    JsonObject json = new JsonObject(jsonOutput);

                    if(json.getString("header").equals("register")){

                        String newId = json.getString("id");

                        vertx.sharedData().getLocalMap("chat.room." + chatRoom).put(newId, newId);
                        users.add(json.getString("id"));

                        logger.info("registering new connection with id: " + newId);
                        logger.info("Number of Currently Connected Users : " + users.size());

                        vertx.eventBus().send(localMap.get(newId).toString(),
                            new JsonObject().put("status", Boolean.TRUE).put("ID", newId));

                    }

                    if(json.getString("header").equals("message")){

                        if(json.containsKey("room")){
                            for(Map.Entry value : localMap.entrySet()){
                                String address = value.getValue().toString();
                                eventBus.send(address, jsonOutput);
                            }
                        }
                        else{
                            eventBus.send(localMap.get(json.getString("receiver")).toString(), jsonOutput);
                        }

                    }

                } catch (IOException e) {
                    ws.reject();
                }
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

}

