package com.nova.simplechat.simplechat;


/**
 * Created by Raji Zakariyya
 *
 * All transfer messages must contain a header.
 */

public class Header {


    private String action;
    private String actor;

    public Header() {}

    public Header(String action) {
        this.action = action;
    }

    public Header(String action, String actor) {
        this.action = action;
        this.actor = actor;
    }

    public String getAction() {
        return action;
    }

    public String getActor() {
        return actor;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }
}
