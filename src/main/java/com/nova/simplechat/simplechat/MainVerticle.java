package com.nova.simplechat.simplechat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nova.data.MongoDB;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
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
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {

        v1();

        new MongoDB(vertx);

    }

    private void v1(){
//        final Pattern chatUrlPattern = Pattern.compile("/chat/(\\w+)");
        final Pattern chatUrlPattern = Pattern.compile("/chat/");
        final EventBus eventBus = vertx.eventBus();
        vertx.createHttpServer().websocketHandler(new Handler<ServerWebSocket>() {
            @Override
            public void handle(final ServerWebSocket ws) {
                final Matcher m = chatUrlPattern.matcher(ws.path());
                if (!m.matches()) {
                    ws.reject();
                    return;
                }

                final String chatRoom = m.group();
//                final String id = ws.textHandlerID();
                final String id = new StringUtils().getSaltString(5);
                System.out.println("registering new connection with id: " + id + " for chat-room: " + chatRoom);
                vertx.sharedData().getLocalMap("chat.room." + chatRoom).put(id, id);
//
                System.out.println("Number of Users in Chatroom : " + vertx.sharedData().getLocalMap("chat.room." + chatRoom).size());

                ws.closeHandler(new Handler<Void>() {
                    @Override
                    public void handle(final Void event) {
                        System.out.println("un-registering connection with id: " + id + " from chat-room: " + chatRoom);
                        vertx.sharedData().getLocalMap("chat.room." + chatRoom).remove(id);
                    }
                });

               //new MessageHandler(MainVerticle.this, ws, chatRoom)

                ws.handler(
                        new Handler<Buffer>() {
                    @Override
                    public void handle(final Buffer data) {

                        ObjectMapper m = new ObjectMapper();
                        try {
                            JsonNode messageFromClient = m.readTree(data.toString());

                            ((ObjectNode) messageFromClient).put("received", new Date().toString());
                            String jsonOutput = m.writeValueAsString(messageFromClient);
                            System.out.println("json generated: " + jsonOutput);

                            LocalMap<Object, Object> localMap = vertx.sharedData().getLocalMap("chat.room." + chatRoom);

                            JsonObject json = new JsonObject(jsonOutput);
                            String receiver = json.getString("receiver");

                            if (json.containsKey("room")) {
                                for (Map.Entry<Object, Object> entry : localMap.entrySet()) {
    //                                Object key = entry.getKey();
                                    Object value = entry.getValue();
                                    System.out.println("Address : " + (String)value);
                                    eventBus.send((String)value, jsonOutput);

                                }
                            }else{
                                eventBus.send(receiver, jsonOutput);
                            }


                        } catch (IOException e) {
                            e.printStackTrace();
                            ws.reject();
                        }
                    }
                });

            }
        }).listen(8080, handler ->{
            if(handler.succeeded()){
                System.out.println("Websocket Chat Server Started on PORT : " + 8080);
            }else{
                handler.cause();
            }
        });//.listen(8080);
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

