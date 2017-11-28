package com.nova.simplechat.simplechat;

import io.vertx.core.http.ServerWebSocket;

/**
 * Created by Raji Zakariyya
 * <p>
 * Passed to a Message Handler for handling messages from an user.
 */

public class Parameters {

    public String data;
    public ServerWebSocket socket;
    public ClientID client;
    public ChatVerticle handler;

    public Parameters(String data, ServerWebSocket socket, ClientID client, ChatVerticle handler) {
        this.data = data;
        this.socket = socket;
        this.client = client;
        this.handler = handler;
    }

    public String getAddress() {
        return socket.textHandlerID();
    }
}
