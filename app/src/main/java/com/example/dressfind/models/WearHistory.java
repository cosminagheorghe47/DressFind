package com.example.dressfind.models;

import java.util.Date;

public class WearHistory {

    private String UserId;
    private String OutfitId;
    private Date WearDate;

    public WearHistory() {
    }

    public WearHistory(String userId, String outfitId, Date wearDate) {
        UserId = userId;
        OutfitId = outfitId;
        WearDate = wearDate;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getOutfitId() {
        return OutfitId;
    }

    public void setOutfitId(String outfitId) {
        OutfitId = outfitId;
    }

    public Date getWearDate() {
        return WearDate;
    }

    public void setWearDate(Date wearDate) {
        WearDate = wearDate;
    }
}
