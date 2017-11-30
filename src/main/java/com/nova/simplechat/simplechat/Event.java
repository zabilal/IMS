package com.nova.simplechat.simplechat;


/**
 * Created by Raji Zakariyya
 * <p>
 * Used to pass data to an Event Handler.
 */
public class Event {


    public String data;
    public ChatVerticle handler;
    public Packet packet;
    public String actor;

    public Event(String data, ChatVerticle handler) {
        this.data = data;
        this.handler = handler;
        this.packet = (Packet) Serializer.unpack(data, Packet.class);
        this.actor = packet.getHeader().getActor();
    }
}
