package com.example.dressfind.services;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.example.dressfind.models.MatchingImage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ImageAnalyzer extends AsyncTask<String, Void, String> {
    private static final String API_KEY = "AIzaSyCSS-WiCPU2UF6Znol71aIw-da6dlQfnz0";
    private static final String VISION_API_URL = "https://vision.googleapis.com/v1/images:annotate?key=" + API_KEY;
    private static final String PLACES_API_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=%d&type=clothing_store&key=%s";
    private static String imageUrl;
    private ProductSearchCallback callback;
    private List<MatchingImage> matchingImages = new ArrayList<>();

    public interface ProductSearchCallback {
        void onMatchingImagesFound(List<MatchingImage> matchingImages);
    }

    public void setCallback(ProductSearchCallback callback) {
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... params) {
        imageUrl = params[0];
        String visionResponse = callVisionApi(imageUrl);
        if (visionResponse == null) return null;

//        JSONArray labelAnnotations = getLabelsFromVisionResponse(visionResponse);
//        List<String> labels = new ArrayList<>();
//        for (int i = 0; i < Math.min(3, labelAnnotations.length()); i++) {
//            String label = null;
//            try {
//                label = labelAnnotations.getJSONObject(i).getString("description");
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            }
//            labels.add(label);
//        }

        double latitude = 44.4268;
        double longitude = 26.1025;
        String placesResponse = getNearbyStores(latitude, longitude);
        if (placesResponse == null) return null;


        String webDetectionResult = getWebDetectionFromImage(imageUrl);
        if (webDetectionResult == null) return null;

        return filterLinksBasedOnStores(placesResponse, webDetectionResult).toString();
    }

    @Override
    protected void onPostExecute(String result) {
        if (callback != null) {
            callback.onMatchingImagesFound(matchingImages);
        }
    }

    private String callVisionApi(String imageUrl) {
        try {
            String requestBody = "{" +
                    "\"requests\": [{" +
                    "\"image\": {\"source\": {\"imageUri\": \"" + imageUrl + "\"}}," +
                    "\"features\": [{" + "\"type\": \"LABEL_DETECTION\"}, {\"type\": \"OBJECT_LOCALIZATION\"}]" + "}]" + "}";
            return makeApiRequest(requestBody);
        } catch (IOException e) {
            Log.e("VisionAPI", "API call failed", e);
            return null;
        }
    }

    private String makeApiRequest(String requestBody) throws IOException {
        URL url = new URL(VISION_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int status = connection.getResponseCode();
        BufferedReader reader;
        if (status >= 300) {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return response.toString();
    }

//    private JSONArray getLabelsFromVisionResponse(String responseJson) {
//        try {
//            JSONObject jsonResponse = new JSONObject(responseJson);
//            JSONArray responses = jsonResponse.getJSONArray("responses");
//            return responses.getJSONObject(0).optJSONArray("labelAnnotations");
//        } catch (JSONException e) {
//            Log.e("VisionAPI", "Error parsing the response", e);
//        }
//        return null;
//    }

    private String getNearbyStores(double latitude, double longitude) {
        try {
            String urlString = String.format(Locale.US, PLACES_API_URL, latitude, longitude, 5000, API_KEY);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int status = connection.getResponseCode();
            BufferedReader reader;
            if (status >= 300) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            return response.toString();
        } catch (IOException e) {
            Log.e("ProductSearch", "Error during Places API call", e);
            return null;
        }
    }

    private String getWebDetectionFromImage(String imageUrl) {
        try {
            String requestBody = "{\"requests\": [{\"image\": {\"source\": {\"imageUri\": \"" + imageUrl + "\"}}," +
                    "\"features\": [{" +
                    "\"type\": \"WEB_DETECTION\"," +
                    "\"maxResults\": " + 300 +
                    "}]}]}";
            return makeApiRequest(requestBody);
        } catch (IOException e) {
            Log.e("ProductSearch", "Error during Vision API call", e);
            return null;
        }
    }

    private List<MatchingImage> filterLinksBasedOnStores(String placesResponse, String webDetectionResult) {
        List<String> storeNames = extractStoreNamesFromPlacesResponse(placesResponse);

        try {
            JSONObject jsonResponse = new JSONObject(webDetectionResult);
            JSONArray responses = jsonResponse.getJSONArray("responses");
            if (responses.length() == 0) return matchingImages;

            JSONObject webDetection = responses.getJSONObject(0).optJSONObject("webDetection");
            if (webDetection == null) return matchingImages;

            JSONArray partialMatchingImages = webDetection.optJSONArray("partialMatchingImages");
            JSONArray pagesWithMatchingImages = webDetection.optJSONArray("pagesWithMatchingImages");

            if (partialMatchingImages != null && pagesWithMatchingImages != null) {
                for (int i = 0; i < Math.min(partialMatchingImages.length(), pagesWithMatchingImages.length()); i++) {
                    String imageUrl = partialMatchingImages.getJSONObject(i).optString("url", "");
                    JSONObject page = pagesWithMatchingImages.getJSONObject(i);
                    String pageTitle = page.optString("pageTitle", "");
                    String pageUrl = page.optString("url", "");
                    matchingImages.add(new MatchingImage(imageUrl, pageTitle, pageUrl));
                    for (String storeName : storeNames) {
                        String concatenatedStoreName = storeName.toLowerCase().replaceAll(" ", "");
                        if (pageTitle.toLowerCase().contains(concatenatedStoreName) ||
                                pageUrl.toLowerCase().contains(concatenatedStoreName) ) {
                            matchingImages.add(new MatchingImage(imageUrl, pageTitle, pageUrl));
                            break;
                        }
                    }
                    if(pageUrl.contains("zalando") ||
                            pageUrl.contains("aboutyou.ro") ||
                            pageUrl.contains("asos") ||
                            pageUrl.contains("puma") ||
                            pageUrl.contains("zara.com/ro") ||
                            pageUrl.contains("bershka.com/ro") ||
                            pageUrl.contains("mango.com/ro")){
                        matchingImages.add(new MatchingImage(imageUrl, pageTitle, pageUrl));
                    }
                }
            }
        } catch (JSONException e) {
            Log.e("ProductSearch", "Error parsing response", e);
        }

        return matchingImages;
    }

    private List<String> extractStoreNamesFromPlacesResponse(String placesResponse) {
        List<String> storeNames = new ArrayList<>();
        try {
            JSONObject jsonResponse = new JSONObject(placesResponse);
            JSONArray results = jsonResponse.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                String name = result.getString("name");
                storeNames.add(name.toLowerCase());
            }
        } catch (JSONException e) {
            Log.e("ProductSearch", "Error parsing response", e);
        }
        return storeNames;
    }


}

