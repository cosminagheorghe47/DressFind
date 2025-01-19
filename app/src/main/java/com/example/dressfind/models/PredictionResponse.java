package com.example.dressfind.models;

import com.google.gson.annotations.SerializedName;


public class PredictionResponse {
    @SerializedName("class")
    private String className;

    @SerializedName("probability")
    private String probability;

    public String getClassName() {
        return className;
    }

    public String getProbability() {
        return probability;
    }
}
