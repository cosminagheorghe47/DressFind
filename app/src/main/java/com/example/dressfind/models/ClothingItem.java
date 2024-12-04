package com.example.dressfind.models;

public class ClothingItem {

    private String ItemId;
    private String UserId;
    private String ScanId;
    private String Name;
    private String Description;
    private String Image;
    private String Material;
    private String Color;
    private Double Price;
    private String StoreId;

    public ClothingItem() {
    }

    public ClothingItem(String itemId, String userId, String scanId, String name, String description, String image, String material, String color, Double price, String storeId) {
        ItemId = itemId;
        UserId = userId;
        ScanId = scanId;
        Name = name;
        Description = description;
        Image = image;
        Material = material;
        Color = color;
        Price = price;
        StoreId = storeId;
    }

    public String getItemId() {
        return ItemId;
    }

    public void setItemId(String itemId) {
        ItemId = itemId;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getScanId() {
        return ScanId;
    }

    public void setScanId(String scanId) {
        ScanId = scanId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getMaterial() {
        return Material;
    }

    public void setMaterial(String material) {
        Material = material;
    }

    public String getColor() {
        return Color;
    }

    public void setColor(String color) {
        Color = color;
    }

    public Double getPrice() {
        return Price;
    }

    public void setPrice(Double price) {
        Price = price;
    }

    public String getStoreId() {
        return StoreId;
    }

    public void setStoreId(String storeId) {
        StoreId = storeId;
    }
}
