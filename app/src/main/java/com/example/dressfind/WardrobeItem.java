package com.example.dressfind;

public class WardrobeItem {

    private String itemId;
    private String color;
    private String description;
    private String image;
    private String material;
    private String name;
    private String scanId;
    private String userId;

    public WardrobeItem() {}

    public WardrobeItem(String itemId, String color, String description, String image,
                        String material, String name, String scanId, String userId) {
        this.itemId = itemId;
        this.color = color;
        this.description = description;
        this.image = image;
        this.material = material;
        this.name = name;
        this.scanId = scanId;
        this.userId = userId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScanId() {
        return scanId;
    }

    public void setScanId(String scanId) {
        this.scanId = scanId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
