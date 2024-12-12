package com.example.dressfind.models;

public class Store {

    private String StoreId;
    private String Name;
    private String WebSite;
    private String ScanId;

    public Store() {
    }

    public Store(String storeId, String name, String webSite, String scanId) {
        StoreId = storeId;
        Name = name;
        WebSite = webSite;
        ScanId = scanId;
    }

    public String getStoreId() {
        return StoreId;
    }

    public void setStoreId(String storeId) {
        StoreId = storeId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getWebSite() {
        return WebSite;
    }

    public void setWebSite(String webSite) {
        WebSite = webSite;
    }

    public String getScanId() {
        return ScanId;
    }

    public void setScanId(String scanId) {
        ScanId = scanId;
    }
}
