package com.nova.simplechat.simplechat;

import io.vertx.core.*;

/**
 * Created by Zakariyya Raji on 05/01/2018.
 * <p>
 * Uploads messages to the logging service.
 */
public class ServerLogger extends AbstractVerticle {
    private Vertx vertx;

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
        vertx.createHttpClient().websocket(Configuration.LOGGER_PORT, "localhost", "/", event -> {

            vertx.eventBus().consumer(Configuration.BUS_LOGGER, data -> {
                vertx.eventBus().send(event.textHandlerID(), data.body().toString());
            });

        });
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {

    }
}
