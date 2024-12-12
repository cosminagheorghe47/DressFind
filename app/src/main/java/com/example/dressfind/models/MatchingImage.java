package com.example.dressfind.models;

public class MatchingImage {
    private String imageUrl;
    private String pageTitle;
    private String pageUrl;

    public MatchingImage(String imageUrl, String pageTitle, String pageUrl) {
        this.imageUrl = imageUrl;
        this.pageTitle = pageTitle;
        this.pageUrl = pageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public String getPageUrl() {
        return pageUrl;
    }
}
