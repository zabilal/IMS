package com.nova.simplechat.simplechat;

import io.vertx.core.*;
import io.vertx.core.http.HttpClient;

/**
 * Created by Raji Zakariyya
 * <p>
 * Handles events from the backend and emits events to the backend.
 */
public class EventVerticle extends AbstractVerticle {

    private static final Integer CONNECTOR_PORT = 5030;
    private Vertx vertx;
    private HttpClient client;

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        connectToBackend();
    }

    // todo reconnect must be set so that operation may resume.
    private void connectToBackend() {
        client = vertx.createHttpClient();

        client.websocket(CONNECTOR_PORT, "localhost", "/", event -> {

            System.out.println("CONNECTED TO THE SERVER BRIDGE " );

            // listen for events from the backend connector service.
            event.handler(data -> {
                vertx.eventBus().send(Configuration.DOWNSTREAM, data);
            });

            // forward emitted events onto the connector.
            vertx.eventBus().consumer(Configuration.UPSTREAM, handler -> {
                vertx.eventBus().send(event.textHandlerID(), handler.body().toString());
            });

            // register this server to the connector for events.
            vertx.eventBus().send(event.textHandlerID(),
                    Serializer.pack(new Register(Configuration.REGISTER_NAME, Configuration.LISTEN_PORT + "")));
        });

//        System.out.println(" " );
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        client.close();
    }
}
