package com.example.dressfind.models;

import java.util.List;

public class DayOutfits {
    private String day;
    private List<Outfit> outfits;

    public DayOutfits(String day, List<Outfit> outfits) {
        this.day = day;
        this.outfits = outfits;
    }

    public String getDay() {
        return day;
    }

    public List<Outfit> getOutfits() {
        return outfits;
    }
}
