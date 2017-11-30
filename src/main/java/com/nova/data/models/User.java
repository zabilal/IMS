package com.nova.data.models;

/**
 * Created by olyjosh on 28/11/2017.
 */
public class User {

    private String username;
//    private String password;
    private String phone;
    private String email;
    private String name;
    private String status;
    private boolean isPublicProfile = false;
    private boolean isAllowOpenRoomInvite = true;
    private boolean isDeletedAccount = false;
    private String [] rooms;

    public User(String username, String phone, String email, String name) {
        this.username = username;
        this.phone = phone;
        this.email = email;
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
