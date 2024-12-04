package com.example.dressfind.models;

public class OutfitsItems {

    private String OutfitId;
    private String ItemId;
    private String Type;

    public OutfitsItems() {
    }

    public OutfitsItems(String outfitId, String itemId, String type) {
        OutfitId = outfitId;
        ItemId = itemId;
        Type = type;
    }

    public String getOutfitId() {
        return OutfitId;
    }

    public void setOutfitId(String outfitId) {
        OutfitId = outfitId;
    }

    public String getItemId() {
        return ItemId;
    }

    public void setItemId(String itemId) {
        ItemId = itemId;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }
}
