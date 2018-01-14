package com.nova.simplechat.simplechat;

/**
 * Created by Raji Zakariyya
 * <p>
 * Message transfer object.
 */

public class Message {

    public static final String ACTION = "message";
    private String sender;
    private String receiver;
    private String content;
    private String room;
    private Header header;
    private Boolean command;
    private String time;
    private boolean me;

    public Message() {
    }


    public Message(String content, String sender, String receiver, String time, boolean isMe){
        this.content = content;
        this.sender = sender;
        this.receiver = receiver;
        this.me = isMe;
        this.time = time;
        this.header = new Header(ACTION);
    }

    public Message( boolean isMe, String content, String sender, String room, String time){
        this.content = content;
        this.sender = sender;
        this.room = room;
        this.me = isMe;
        this.time = time;
        this.header = new Header(ACTION);
    }

    public Message resetHeader() {
        this.header = new Header(ACTION);
        return this;
    }

    public String getContent() {
        return content;
    }

    public Message setContent(String content) {
        this.content = content;
        return this;
    }

    public String getSender() {
        return sender;
    }

    public Message setSender(String sender) {
        this.sender = sender;
        return this;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public String getRoom() {
        return room;
    }

    public Message setRoom(String room) {
        this.room = room;
        return this;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isMe() {
        return me;
    }

    public void setMe(boolean me) {
        this.me = me;
    }

    public Message setReceiver(String receiver){
        this.receiver = receiver;
        return this;
    }

    public String getReceiver(){return receiver;}

    public Boolean getCommand() {
        return command;
    }

    public Message setCommand(Boolean command) {
        this.command = command;
        return this;
    }


}
