package com.nova.simplechat.simplechat;

public class RoomFinder {

    public static final String ACTION = "roomfinder";
    private String sender;
    private Header header;

    public RoomFinder() {
        this.header = new Header(ACTION);
    }

    public RoomFinder(String sender){
        this.sender = sender;
        this.header = new Header(ACTION);
    }

    public static String getACTION() {
        return ACTION;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }
}
