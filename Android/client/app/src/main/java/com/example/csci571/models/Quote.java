package com.example.csci571.models;

public class Quote {
    private double c;
    private double d;
    private double dp;
    private double h;
    private double l;
    private double o;
    private double pc;
    private double t;

    public Quote(double c, double d, double dp, double h, double l, double o, double pc, double t) {
        this.c = c;
        this.d = d;
        this.dp = dp;
        this.h = h;
        this.l = l;
        this.o = o;
        this.pc = pc;
        this.t = t;
    }

    public double getC() { return c; }

    public double getD() {
        return d;
    }

    public double getDp() {
        return dp;
    }

    public double getH() {
        return h;
    }

    public double getL() {
        return l;
    }

    public double getO() {
        return o;
    }

    public double getPc() {
        return pc;
    }

    public double getT() {
        return t;
    }
}
