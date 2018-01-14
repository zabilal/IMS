package com.nova.simplechat.simplechat;

public class OnlineContact {

    public static final String ACTION = "onlinecontact";
    private String contacts;
    private String sender;
    private Header header;

    public OnlineContact() {
    }

    public OnlineContact(String contacts, String sender){
        this.contacts = contacts;
        this.sender = sender;
        this.header = new Header(ACTION);
    }

    public static String getACTION() {
        return ACTION;
    }

    public String getContacts() {
        return contacts;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
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
