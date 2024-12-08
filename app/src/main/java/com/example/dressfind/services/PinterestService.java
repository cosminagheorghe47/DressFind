package com.example.dressfind.services;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.example.dressfind.models.PinterestProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;

public class PinterestService {

    private final SharedPreferencesService sharedPreferencesService;
    private static final String BASE_URL = "https://api.pinterest.com/v5";
    private static final String TAG = "PinterestAPI";

    public PinterestService(Context context) {
        this.sharedPreferencesService = new SharedPreferencesService(context);
    }

    public void exchangeAuthCodeForToken(String authCode, TokenCallback callback) {
        OkHttpClient client = createClient();

        String credentials = "1509034:e543c8a4a6e6d2dc064f6bcca661392ff656884c";
        String authHeader = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        RequestBody body = new FormBody.Builder()
                .add("code", authCode)
                .add("redirect_uri", "com.example.dressfind://home/")
                .add("grant_type", "authorization_code")
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "/oauth/token")
                .post(body)
                .addHeader("Authorization", authHeader)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Token Exchange Failure", e);
                if (callback != null) callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Log.d(TAG, "Token response: " + body);
                    try {
                        JSONObject json = new JSONObject(body);
                        String accessToken = json.getString("access_token");
                        String refreshToken = json.getString("refresh_token");
                        long accessTokenExpiresIn = json.getLong("expires_in");
                        long refreshTokenExpiresIn = json.getLong("refresh_token_expires_in");

                        long currentTime = System.currentTimeMillis() / 1000;
                        long accessTokenExpiry = currentTime + accessTokenExpiresIn;
                        long refreshTokenExpiry = currentTime + refreshTokenExpiresIn;

                        sharedPreferencesService.saveAccessToken(accessToken);
                        sharedPreferencesService.saveRefreshToken(refreshToken);
                        sharedPreferencesService.saveAccessTokenExpiry(accessTokenExpiry);
                        sharedPreferencesService.saveRefreshTokenExpiry(refreshTokenExpiry);
                        Log.e(TAG, accessTokenExpiry + "         " + refreshTokenExpiry);
                        if (callback != null) callback.onTokenReceived(accessToken);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Parsing Error", e);
                        if (callback != null) callback.onError("Invalid JSON");
                    }
                } else {
                    Log.e(TAG, "Token Exchange Error: " + response.message());
                    if (callback != null) callback.onError("Token exchange failed");
                }
            }
        });
    }

    public void fetchUserProfile(String accessToken, ProfileCallback callback) {
        if (accessToken == null || accessToken.isEmpty()) {
            if (callback != null) callback.onError("Access token is missing");
            return;
        }

        OkHttpClient client = createClient();
        Request request = new Request.Builder()
                .url(BASE_URL + "/user_account")
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Profile Fetch Failure", e);
                if (callback != null) callback.onError(e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Log.e(TAG, body);
                    try {
                        PinterestProfile profile = parseProfile(body);
                        if (callback != null) callback.onProfileFetched(profile);
                    } catch (JSONException e) {
                        Log.e(TAG, "Profile Parsing Error", e);
                        if (callback != null) callback.onError("Invalid profile JSON");
                    }
                } else {
                    Log.e(TAG, "Profile Fetch Error: " + response.message());
                    if (callback != null) callback.onError("Profile fetch failed");
                }
            }
        });
    }
//E  {"account_type":"BUSINESS","username":"DressFindAccount","following_count":0,"website_url":"","monthly_views":-1,
// "profile_image":"https://i.pinimg.com/600x600_R/94/40/81/94408195dffeef7079e814dbd3f07769.jpg","id":"967359332369035258",
// "about":"","business_name":"Facultatea de Matematica si Informatica","follower_count":0,"pin_count":0,"board_count":0}
    private PinterestProfile parseProfile(String json) throws JSONException {
        JSONObject object = new JSONObject(json);
        PinterestProfile profile = new PinterestProfile();
        profile.setUsername(object.getString("username"));
        profile.setProfileImage(object.getString("profile_image"));
        profile.setFollowerCount(object.getInt("follower_count"));
        profile.setBoardCount(object.getInt("board_count"));
        profile.setAbout(object.getString("about"));
        profile.setBusinessName(object.getString("business_name"));
        profile.setPinCount(object.getInt("pin_count"));
        return profile;
    }
    public void refreshAccessToken(String refreshToken, TokenCallback callback) {
        OkHttpClient client = createClient();

        String credentials = "1509034:e543c8a4a6e6d2dc064f6bcca661392ff656884c";
        String authHeader = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        RequestBody body = new FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "/oauth/token")
                .post(body)
                .addHeader("Authorization", authHeader)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Token Refresh Failure", e);
                if (callback != null) callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Log.d(TAG, "Token response: " + body);
                    try {
                        JSONObject json = new JSONObject(body);
                        String accessToken = json.getString("access_token");
                        String refreshToken = json.getString("refresh_token");
                        long accessTokenExpiresIn = json.getLong("expires_in");
                        long refreshTokenExpiresIn = json.getLong("refresh_token_expires_in");

                        long currentTime = System.currentTimeMillis() / 1000;
                        long accessTokenExpiry = currentTime + accessTokenExpiresIn;
                        long refreshTokenExpiry = currentTime + refreshTokenExpiresIn;

                        sharedPreferencesService.saveAccessToken(accessToken);
                        sharedPreferencesService.saveRefreshToken(refreshToken);
                        sharedPreferencesService.saveAccessTokenExpiry(accessTokenExpiry);
                        sharedPreferencesService.saveRefreshTokenExpiry(refreshTokenExpiry);

                        if (callback != null) callback.onTokenReceived(accessToken);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Parsing Error", e);
                        if (callback != null) callback.onError("Invalid JSON");
                    }
                } else {
                    Log.e(TAG, "Token Refresh Error: " + response.message());
                    if (callback != null) callback.onError("Token refresh failed");
                }
            }
        });
    }

    private OkHttpClient createClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
    }

    public interface TokenCallback {
        void onTokenReceived(String accessToken);

        void onError(String error);
    }

    public interface ProfileCallback {
        void onProfileFetched(PinterestProfile profile);

        void onError(String error);
    }
}
