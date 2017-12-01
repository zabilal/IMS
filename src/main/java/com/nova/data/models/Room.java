package com.nova.data.models;

/**
 * Created by olyjosh on 30/11/2017.
 */
public class Room {

    private String title;
    private String owner;
    private String serverId;
    private String [] admins;

    public Room(String title, String owner) {
        this.title = title;
        this.owner = owner;
    }

    public Room(String title, String owner, String serverId) {
        this.title = title;
        this.owner = owner;
        this.serverId = serverId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String[] getAdmins() {
        return admins;
    }

    public void setAdmins(String[] admins) {
        this.admins = admins;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
}
