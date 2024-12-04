package com.example.dressfind.models;

public class FavoriteOutfits {

    private String UserId;
    private String OutfitId;

    public FavoriteOutfits() {
    }

    public FavoriteOutfits(String userId, String outfitId) {
        UserId = userId;
        OutfitId = outfitId;
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
}
