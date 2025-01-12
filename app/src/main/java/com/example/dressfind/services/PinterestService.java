package com.example.dressfind.services;

import android.content.Context;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.dressfind.models.CreatePinResponse;
import com.example.dressfind.models.PinterestProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
                        String refreshToken = json.optString("refresh_token", null);

                        long accessTokenExpiresIn = json.getLong("expires_in");
                        long refreshTokenExpiresIn = json.optLong("refresh_token_expires_in");
                        long refreshTokenExpiry;
                        long currentTime = System.currentTimeMillis() / 1000;
                        long accessTokenExpiry = currentTime + accessTokenExpiresIn;
                        if (refreshToken ==null) {
                            Log.e(TAG, "Token Refreshed but did not return a refresh token(Pinterest doesnt always return one)");
                             refreshTokenExpiry = currentTime + 5184000;
                        }
                        else{
                            refreshTokenExpiry = currentTime + refreshTokenExpiresIn;
                        }
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
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
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

    public void createPin(String boardId, String publicFirebaseImageUrl, String title, String description, CreatePinCallback callback) {
        // Check the image dimensions before proceeding
        ImageUploader.checkImageSize(publicFirebaseImageUrl, (width, height) -> {
            if (width < 400 || height < 400 || width > 3000 || height > 3000) {
                Log.d(TAG, "Image size out of allowed range. Resizing...");
                // Resize the image to fit within the allowed dimensions
                ImageUploader.resizeImage(publicFirebaseImageUrl, 400, 3000, new ImageUploader.ResizeCallback() {
                    @Override
                    public void onResized(String resizedImageUrl) {
                        Log.d(TAG, "Image resized successfully. Attempting to create pin...");
                        createPinInternal(boardId, resizedImageUrl, title, description, callback);
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Image resizing failed: " + error);
                        if (callback != null) callback.onError("Image resizing failed: " + error);
                    }
                });
            } else {
                Log.d(TAG, "Image size is within the allowed range." + width + "   "+ height+"   Proceeding to create pin...");
                createPinInternal(boardId, publicFirebaseImageUrl, title, description, callback);
            }
        }, error -> {
            Log.e(TAG, "Failed to check image size: " + error);
            if (callback != null) callback.onError("Failed to check image size: " + error);
        });
    }

    private void createPinInternal(String boardId, String imageUrl, String title, String description, CreatePinCallback callback) {
        // Existing logic to create a pin
        OkHttpClient client = createClient();
        JSONObject mediaSourceBody = new JSONObject();
        JSONObject pinDetails = new JSONObject();
        try {
            pinDetails.put("link", "https://www.pinterest.com/");
            pinDetails.put("title", title);
            pinDetails.put("description", description);
            pinDetails.put("board_id", boardId);

            mediaSourceBody.put("source_type", "image_url");
            mediaSourceBody.put("url", imageUrl);
            mediaSourceBody.put("is_standard", true);

            pinDetails.put("media_source", mediaSourceBody);
        } catch (JSONException e) {
            Log.e(TAG, "JSON Error", e);
            if (callback != null) callback.onError("Invalid JSON structure");
            return;
        }
        Log.e(TAG, "Token " + sharedPreferencesService.getAccessToken());
        RequestBody body = RequestBody.create(pinDetails.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url("https://api-sandbox.pinterest.com/v5" + "/pins")
                .post(body)
                .addHeader("Authorization", "Bearer " + "pina_AMA2UBQXAD5O6AYAGDAGYDMBEENVPFABACGSOPUK4DJ4KWTT6ZYYQEUW6PFPURCC6W3UJPREEWR45XIUZBEWDVWR3EEFCPYA")
                .addHeader("Content-Type", "application/json")
                .build();
        Log.e(TAG, "Body " + pinDetails.toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Create Pin Failure", e);
                if (callback != null) callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        CreatePinResponse pin = parseCreatePinResponse(responseBody);
                        if (callback != null) callback.onSuccess(pin);
                    } catch (JSONException e) {
                        Log.e(TAG, "Pin Parsing Error", e);
                        if (callback != null) callback.onError("Invalid Pin JSON");
                    }
                } else {
                    Log.e(TAG, "Create Pin Error: " + response.code());
                    if (callback != null) callback.onError("Create Pin failed: " + response.toString() + "  " + response.body() + "  " );
                }
            }
        });
    }

    public interface CreatePinCallback {
        void onSuccess(CreatePinResponse createPinResponse);

        void onError(String error);
    }

    private CreatePinResponse parseCreatePinResponse(String json) throws JSONException {
        JSONObject object = new JSONObject(json);
        CreatePinResponse pin = new CreatePinResponse();

        pin.setId(object.getString("id"));
        pin.setCreatedAt(object.getString("created_at"));
        pin.setLink(object.getString("link"));
        pin.setTitle(object.getString("title"));
        pin.setDescription(object.getString("description"));
        pin.setDominantColor(object.optString("dominant_color"));
        pin.setAltText(object.optString("alt_text"));
        pin.setCreativeType(object.optString("creative_type"));
        pin.setBoardId(object.getString("board_id"));
        pin.setBoardOwnerUsername(object.getJSONObject("board_owner").getString("username"));
        pin.setOwner(object.optBoolean("is_owner"));
        pin.setStandard(object.optBoolean("is_standard"));

        // Media Images
        if (object.has("media") && object.getJSONObject("media").has("images")) {
            JSONObject images = object.getJSONObject("media").getJSONObject("images");
            pin.setMediaImages(parseMediaImages(images));
        }


        return pin;
    }

    private Map<String, Map<String, Object>> parseMediaImages(JSONObject images) throws JSONException {
        Map<String, Map<String, Object>> mediaImages = new HashMap<>();
        for (Iterator<String> it = images.keys(); it.hasNext(); ) {
            String size = it.next();
            JSONObject imageDetails = images.getJSONObject(size);
            Map<String, Object> imageMap = new HashMap<>();
            imageMap.put("width", imageDetails.getInt("width"));
            imageMap.put("height", imageDetails.getInt("height"));
            imageMap.put("url", imageDetails.getString("url"));
            mediaImages.put(size, imageMap);
        }
        return mediaImages;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String encodeImageToBase64(String imagePath) throws IOException {
        byte[] fileContent = Files.readAllBytes(Paths.get(imagePath));
        return java.util.Base64.getEncoder().encodeToString(fileContent);
    }

}
