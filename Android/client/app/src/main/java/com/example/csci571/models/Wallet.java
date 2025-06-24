package com.example.csci571.models;

public class Wallet {
    private String id;
    private double money;

    public Wallet(String id, Double money) {
        this.id = id;
        this.money = money;
    }

    public String getId() {
        return id;
    }

    public Double getMoney() {
        return money;
    }
}
