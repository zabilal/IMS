package com.nova.simplechat.simplechat;


/**
 * Created by Raji Zakariyya
 * <p>
 * Contains client parameters.
 */


public class ClientID {

    private String room;
    private String id;
    private String phoneNumber;
    private String username;
    private boolean authenticated = false;

    public ClientID(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
