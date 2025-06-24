package com.example.csci571.models;

public class News {
    String category;
    int datetime;
    String headline;
    int id;
    String image;
    String related;
    String source;
    String summary;
    String url;

    public News(String category, int datetime, String headline, int id, String image, String related, String source, String summary, String url) {
        this.category = category;
        this.datetime = datetime;
        this.headline = headline;
        this.id = id;
        this.image = image;
        this.related = related;
        this.source = source;
        this.summary = summary;
        this.url = url;
    }

    public String getCategory() {
        return category;
    }

    public int getDatetime() {
        return datetime;
    }

    public String getHeadline() {
        return headline;
    }

    public int getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public String getRelated() {
        return related;
    }

    public String getSource() {
        return source;
    }

    public String getSummary() {
        return summary;
    }

    public String getUrl() {
        return url;
    }
}
