package com.nova.simplechat.simplechat;

import java.util.ArrayList;


/**
 * Created by Raji Zakariyya
 * <p>
 * Transfer object for querying a room or returning a query response.
 */


public class Room {


    public static final String ACTION = "room";
    private String topic;
    private String room;
    private String version;
    private String owner;
    private Header header;
    private String username;
    private Boolean created = false;
    private Boolean errorInsideAlready;
    private Boolean system = false;
    private ArrayList<Message> history = new ArrayList<>();

    public Room() {
        this.header = new Header(ACTION);
    }


    public Room(String room, String topic) {
        this(room, topic, null, null);
    }

    public Room(String room, String topic, String owner, String id) {
        this.room = room;
        this.topic = topic;
        this.username = owner;
        this.header = new Header(ACTION, id);
    }

    public Room(String room, String topic, String owner) {
        this.room = room;
        this.topic = topic;
        this.username = owner;
        this.header = new Header(ACTION);
    }

    public Room(Room room, Boolean created) {
        this(room.getRoom(), room.getTopic(), room.getOwner(), null);
        this.created = created;
    }

    public String getOwner() {
        return owner;
    }

    public Room setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public Boolean getErrorInsideAlready() {
        return errorInsideAlready;
    }

    public Room setErrorInsideAlready(Boolean errorInsideAlready) {
        this.errorInsideAlready = errorInsideAlready;
        return this;
    }

    public Boolean getCreated() {
        return created;
    }

    public void setCreated(Boolean created) {
        this.created = created;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getRoom() {
        return room;
    }

    public Room setRoom(String name) {
        this.room = name;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Boolean getSystem() {
        return system;
    }

    public Room setSystem(Boolean system) {
        this.system = system;
        return this;
    }

    public ArrayList<Message> getHistory() {
        return history;
    }

    public void setHistory(ArrayList<Message> history) {
        this.history = history;
    }

    @Override
    public String toString() {
        return "Room{" +
            "topic='" + topic + '\'' +
            ", room='" + room + '\'' +
            ", version='" + version + '\'' +
            ", owner='" + owner + '\'' +
            ", header=" + header +
            ", username='" + username + '\'' +
            ", created=" + created +
            ", errorInsideAlready=" + errorInsideAlready +
            ", system=" + system +
            ", history=" + history +
            '}';
    }
}
