package com.example.dressfind.services;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesService {

    private static final String PREFS_NAME = "DressFindPrefs";
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String REFRESH_TOKEN_KEY = "refresh_token";
    private static final String ACCESS_TOKEN_EXPIRY_KEY = "access_token_expires_in";
    private static final String REFRESH_TOKEN_EXPIRY_KEY = "refresh_token_expires_in";

    private final SharedPreferences sharedPreferences;

    public SharedPreferencesService(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveAccessTokenExpiry(long expiryTimestamp) {
        sharedPreferences.edit().putLong(ACCESS_TOKEN_EXPIRY_KEY, expiryTimestamp).apply();
    }

    public long getAccessTokenExpiry() {
        return sharedPreferences.getLong(ACCESS_TOKEN_EXPIRY_KEY, 0);
    }

    public void saveRefreshTokenExpiry(long expiryTimestamp) {
        sharedPreferences.edit().putLong(REFRESH_TOKEN_EXPIRY_KEY, expiryTimestamp).apply();
    }
    public boolean isAccessTokenExpired() {
        long currentTime = System.currentTimeMillis() / 1000;
        return currentTime >= getAccessTokenExpiry();
    }

    public boolean isRefreshTokenExpired() {
        long currentTime = System.currentTimeMillis() / 1000;
        return currentTime >= getRefreshTokenExpiry();
    }
    public long getRefreshTokenExpiry() {
        return sharedPreferences.getLong(REFRESH_TOKEN_EXPIRY_KEY, 0);
    }


    // Generic save and get methods
    public void saveString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    // Save and get specific tokens
    public void saveAccessToken(String accessToken) {
        saveString(ACCESS_TOKEN_KEY, accessToken);
    }

    public String getAccessToken() {
        return getString(ACCESS_TOKEN_KEY, null);
    }

    public void saveRefreshToken(String refreshToken) {
        saveString(REFRESH_TOKEN_KEY, refreshToken);
    }

    public String getRefreshToken() {
        return getString(REFRESH_TOKEN_KEY, null);
    }


    public void clearPreferences() {
        sharedPreferences.edit().clear().apply();
    }
}
