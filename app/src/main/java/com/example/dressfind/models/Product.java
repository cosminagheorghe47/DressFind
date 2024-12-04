package com.example.dressfind.models;

public class Product {

    private String ProductId;
    private String ScanId;
    private String Description;
    private String Image;
    private String Material;
    private String Color;
    private Double Price;
    private String StoreID;

    public Product() {
    }

    public Product(String productId, String scanId, String description, String image, String material, String color, Double price, String storeID) {
        ProductId = productId;
        ScanId = scanId;
        Description = description;
        Image = image;
        Material = material;
        Color = color;
        Price = price;
        StoreID = storeID;
    }

    public String getProductId() {
        return ProductId;
    }

    public void setProductId(String productId) {
        ProductId = productId;
    }

    public String getScanId() {
        return ScanId;
    }

    public void setScanId(String scanId) {
        ScanId = scanId;
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

    public String getStoreID() {
        return StoreID;
    }

    public void setStoreID(String storeID) {
        StoreID = storeID;
    }
}
