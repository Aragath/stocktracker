package com.example.csci571.models;

public class Favorite {
    private String id;
    private String ticker;
    private String name;

    public Favorite(String id, String ticker, String name) {
        this.id = id;
        this.ticker = ticker;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getTicker() {
        return ticker;
    }

    public String getName() {
        return name;
    }
}
