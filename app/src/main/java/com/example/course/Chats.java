package com.example.course;

public class Chats {
    private String id;
    private String chatId;
    private String source;
    private String destination;
    private String lastMessage;

    public Chats(String destination, String lastMessage, String chatId) {
        this.destination = destination;
        this.lastMessage = lastMessage;
        this.chatId = chatId;
    }

    public Chats() {}

    public Chats(String destination, String lastMessage){
        this.destination = destination;
        this.lastMessage = lastMessage;
    }

    public Chats(String destination, String lastMessage, String id, String chatId) {
        this.destination = destination;
        this.lastMessage = lastMessage;
        this.id = id;
        this.chatId = chatId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
