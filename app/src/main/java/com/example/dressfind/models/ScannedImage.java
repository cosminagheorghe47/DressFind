package com.example.dressfind.models;

import java.util.Date;

public class ScannedImage {

    private String ScanId;
    private String UserId;
    private String Image;
    private Date ScanDate;

    public ScannedImage() {
    }

    public ScannedImage(String scanId, String userId, String image, Date scanDate) {
        ScanId = scanId;
        UserId = userId;
        Image = image;
        ScanDate = scanDate;
    }

    public String getScanId() {
        return ScanId;
    }

    public void setScanId(String scanId) {
        ScanId = scanId;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public Date getScanDate() {
        return ScanDate;
    }

    public void setScanDate(Date scanDate) {
        ScanDate = scanDate;
    }
}
