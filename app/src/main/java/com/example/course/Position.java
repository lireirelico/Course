package com.example.course;

public class Position {
    private String name;
    private int level;
    private String id;

    public Position(){}

    public Position(String name, int level, String id) {
        this.name = name;
        this.level = level;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setName(String name) {
        this.name = name;
    }
}
