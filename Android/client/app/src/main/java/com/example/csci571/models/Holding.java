package com.example.csci571.models;

public class Holding {
    private String id;
    private String ticker;
    private String name;
    private int quantity;
    private double cost;

    public Holding(String id, String ticker, String name, int quantity, double cost) {
        this.id = id;
        this.ticker = ticker;
        this.name = name;
        this.quantity = quantity;
        this.cost = cost;
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

    public int getQuantity() {
        return quantity;
    }

    public double getCost() {
        return cost;
    }
}
