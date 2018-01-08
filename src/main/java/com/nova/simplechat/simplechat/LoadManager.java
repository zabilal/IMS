package com.nova.simplechat.simplechat;

import io.vertx.core.Vertx;

/**
 * Created by Zakariyya Raji on 05/01/2018.
 * <p>
 * Provides a delta-buffer for downscaling to avoid fluctuation
 * and prevent fragmentation.
 * <p>
 */
class LoadManager {
    private Vertx vertx;
    private Boolean full = false;

    public LoadManager(Vertx vertx) {
        this.vertx = vertx;
    }

    /**
     * Evaluates whether the server should notify the system to
     * stop routing users to it. This is done during heavy load.
     *
     * @param users Number of currently connected users.
     */
    public void manage(Integer users) {
        if (Configuration.LOAD_MAX_USERS <= users && !full) {
            sendBus(Configuration.UPSTREAM, new ServerEvent(ServerEvent.ServerStatus.FULL));
            full = true;
        }

        if (users <= Configuration.LOAD_MAX_USERS - Configuration.LOAD_DELTA_BUFFER && full) {
            sendBus(Configuration.UPSTREAM, new ServerEvent(ServerEvent.ServerStatus.READY));
            full = false;
        }
    }

    private void sendBus(String address, Object message) {
        vertx.eventBus().send(address, Serializer.pack(message));
    }
}
