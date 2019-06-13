package com.example.course;

public class Message {

    private String id;
    private String text;
    private String name;
    private String photoUrl;
    private String imageUrl;
    private String side;
    private String time;

    public Message() {
    }

    public Message(String text, String side, String time) {
        this.text = text;
        this.side = side;
        this.time = time;
    }

    public Message(String text, String side, String time, String imageUrl) {
        this.text = text;
        this.side = side;
        this.time = time;
        this.imageUrl = imageUrl;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) { this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getText() {
            return text;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
