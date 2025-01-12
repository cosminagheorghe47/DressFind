package com.example.dressfind.models;

import android.os.Parcel;
import android.os.Parcelable;

public class WardrobeItem implements Parcelable {

    private String itemId;
    private String color;
    private String description;
    private String image;
    private String material;
    private String name;
    private String scanId;
    private String userId;
    private String Category;

    public WardrobeItem() {}

    public WardrobeItem(String itemId, String color, String description, String image,
                        String material, String name, String scanId, String userId, String Category) {
        this.itemId = itemId;
        this.color = color;
        this.description = description;
        this.image = image;
        this.material = material;
        this.name = name;
        this.scanId = scanId;
        this.userId = userId;
        this.Category = Category;
    }

    protected WardrobeItem(Parcel in) {
        itemId = in.readString();
        color = in.readString();
        description = in.readString();
        image = in.readString();
        material = in.readString();
        name = in.readString();
        scanId = in.readString();
        userId = in.readString();
        Category = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(itemId);
        dest.writeString(color);
        dest.writeString(description);
        dest.writeString(image);
        dest.writeString(material);
        dest.writeString(name);
        dest.writeString(scanId);
        dest.writeString(userId);
        dest.writeString(Category);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<WardrobeItem> CREATOR = new Creator<WardrobeItem>() {
        @Override
        public WardrobeItem createFromParcel(Parcel in) {
            return new WardrobeItem(in);
        }

        @Override
        public WardrobeItem[] newArray(int size) {
            return new WardrobeItem[size];
        }
    };

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

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }
}
