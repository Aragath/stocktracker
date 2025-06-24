package com.example.csci571.models;

public class Profile {
    private String country;
    private String currency;
    private String estimateCurrency;
    private String exchange;
    private String finnhubIndustry;
    private String ipo;
    private String logo;
    private double marketCapitalization;
    private String name;
    private String phone;
    private double shareOutstanding;
    private String ticker;
    private String weburl;

    public Profile(String country, String currency, String estimateCurrency, String exchange, String finnhubIndustry, String ipo, String logo, double marketCapitalization, String name, String phone, double shareOutstanding, String ticker, String weburl) {
        this.country = country;
        this.currency = currency;
        this.estimateCurrency = estimateCurrency;
        this.exchange = exchange;
        this.finnhubIndustry = finnhubIndustry;
        this.ipo = ipo;
        this.logo = logo;
        this.marketCapitalization = marketCapitalization;
        this.name = name;
        this.phone = phone;
        this.shareOutstanding = shareOutstanding;
        this.ticker = ticker;
        this.weburl = weburl;
    }

    public String getCountry() {
        return country;
    }

    public String getCurrency() {
        return currency;
    }

    public String getEstimateCurrency() {
        return estimateCurrency;
    }

    public String getExchange() {
        return exchange;
    }

    public String getFinnhubIndustry() {
        return finnhubIndustry;
    }

    public String getIpo() {
        return ipo;
    }

    public String getLogo() {
        return logo;
    }

    public double getMarketCapitalization() {
        return marketCapitalization;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public double getShareOutstanding() {
        return shareOutstanding;
    }

    public String getTicker() {
        return ticker;
    }

    public String getWeburl() {
        return weburl;
    }
}
