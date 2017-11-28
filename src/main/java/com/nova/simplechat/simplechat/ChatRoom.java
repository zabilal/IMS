package com.nova.simplechat.simplechat;

import java.util.HashMap;


/**
 * Created by Raji Zakariyya
 * <p>
 * Defines a room, of connected clients and metadata.
 */


public class ChatRoom {


    private HashMap<String, ClientID> clients = new HashMap<>();
    private Room settings = new Room();

    public ChatRoom(Room room) {
        this(room.getRoom(), room.getTopic(), room.getOwner());
        this.settings.setHistory(room.getHistory());
    }

    public ChatRoom(String name, String topic, String username) {
        settings.setRoom(name);
        settings.setTopic(topic);
        settings.setOwner(username);
    }

    public HashMap<String, ClientID> getClients() {
        return clients;
    }

    public void remove(ClientID client) {
        clients.remove(client.getId());
    }

    public ClientID get(String id) {
        return clients.get(id);
    }

    public void add(ClientID client) {
        clients.put(client.getId(), client);
    }

    public void setSettings(Room settings) {
        this.settings = settings;
    }

    public Room getSettings() {
        return settings;
    }

    public String getOwner() {
        return settings.getOwner();
    }

    public void addHistory(Message message) {
        if (settings.getHistory().size() >= Configuration.MAX_HISTORY)
            settings.getHistory().remove(0);

        settings.getHistory().add(message);
    }


}
