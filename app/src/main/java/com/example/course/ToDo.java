package com.example.course;

public class ToDo {

    String id;
    String description;
    String title;
    String sender;
    boolean me;

    public ToDo() {
        this.id = id;
        this.description = description;
        this.title = title;
        this.sender = sender;
    }

    public ToDo(String todoID, String description, String title, String sender) {
        this.id = todoID;
        this.description = description;
        this.sender = sender;
        this.title = title;
    }

    public ToDo(String todoID, String description, String title, String sender, boolean me) {
        this.id = todoID;
        this.description = description;
        this.sender = sender;
        this.title = title;
        this.me = me;
    }
    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getSender() {
        return sender;
    }

    public String getTitle() {
        return title;
    }

    public boolean isMe() {
        return me;
    }

    public void setMe(boolean me) {
        this.me = me;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setId(String todoID) {
        this.id = todoID;
    }
}

