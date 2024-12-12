package com.example.dressfind.services;

import android.os.AsyncTask;
import android.util.Log;

import com.example.dressfind.activities.MainActivity;
import com.example.dressfind.models.MatchingImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductSearchTask extends AsyncTask<String, Void, String> {
    private static final String CSE_ID = "d7b41b2628af548b7";
    private static final String API_KEY = "AIzaSyCSS-WiCPU2UF6Znol71aIw-da6dlQfnz0";
    private static final String VISION_API_URL = "https://vision.googleapis.com/v1/images:annotate?key=" + API_KEY;
    private static final String PLACES_API_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=%d&type=clothing_store&key=%s";
    List<MatchingImage> matchingImages = new ArrayList<>();
    private ProductSearchCallback callback;
    public interface ProductSearchCallback {
        void onMatchingImagesFound(List<MatchingImage> matchingImages);
    }

    public void setCallback(ProductSearchCallback callback) {
        this.callback = callback;
    }
    private String getNearbyStores(double latitude, double longitude) {
        try {
            String urlString = String.format(Locale.US, PLACES_API_URL, latitude, longitude, 5000, API_KEY);
            Log.d("TEST", urlString);
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
        } catch (IOException e) {
            Log.e("ProductSearch", "Error during Vision API call", e);
            return null;
        }
    }

    @Override
    protected String doInBackground(String... params) {
        String imageUrl = params[0];
        double latitude = 44.4268;
        double longitude = 26.1025;
        String placesResponse = getNearbyStores(latitude, longitude);
        Log.d("Results", placesResponse);
        if (placesResponse == null) {
            return null;
        }
        String webDetectionResult = getWebDetectionFromImage(imageUrl);
        Log.d("Results", webDetectionResult);
        if (webDetectionResult == null) {
            return null;
        }
        return filterLinksBasedOnStores(placesResponse, webDetectionResult).toString();
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {

            if (callback != null) {
                callback.onMatchingImagesFound(matchingImages);
            }
            Log.d("ProductSearch", "Filtered Links: " + result);
        } else {
            Log.e("ProductSearch", "No links found");
        }
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

                    for (String storeName : storeNames) {
                        String concatenatedStoreName = storeName.toLowerCase().replaceAll(" ", "");
                        if (pageTitle.toLowerCase().contains(concatenatedStoreName) ||
                                pageUrl.toLowerCase().contains(concatenatedStoreName) ||
                                pageUrl.contains("zalando") ||
                                pageUrl.contains("aboutyou.ro") ||
                                pageUrl.contains("asos") ||
                                pageUrl.contains("puma") ||
                                pageUrl.contains("zara.com/ro") ||
                                pageUrl.contains("bershka.com/ro") ||
                                pageUrl.contains("mango.com/ro")) {
                            matchingImages.add(new MatchingImage(imageUrl, pageTitle, pageUrl));

                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e("ProductSearch", "Error parsing response", e);
        }

        return matchingImages;
    }


}


//package com.example.dressfind.services;
//
//import android.os.AsyncTask;
//import android.util.Log;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.List;
//
//public class ProductSearchTask extends AsyncTask<List<String>, Void, String> {
//    private static final String CSE_ID = "d7b41b2628af548b7";
//    private static final String API_KEY = "AIzaSyCSS-WiCPU2UF6Znol71aIw-da6dlQfnz0";
//    private static final String SEARCH_API_URL = "https://www.googleapis.com/customsearch/v1?q=%s+buy+store+shopping&key=%s&cx=%s";
//    private static final String PLACES_API_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=5000&type=store&key=%s";
//
//    private String getNearbyStores(double latitude, double longitude) {
//        try {
//            String urlString = String.format(PLACES_API_URL, latitude, longitude, API_KEY);
//            URL url = new URL(urlString);
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("GET");
//
//            int status = connection.getResponseCode();
//            BufferedReader reader;
//            if (status > 299) {
//                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
//            } else {
//                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            }
//
//            StringBuilder response = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                response.append(line);
//            }
//            reader.close();
//
//            return response.toString();
//        } catch (IOException e) {
//            Log.e("ProductSearch", "Error during Places API call", e);
//            return null;
//        }
//    }
//
//    private String searchProductsAtStores(String refinedQuery) {
//        try {
//            String urlString = String.format(SEARCH_API_URL, refinedQuery, API_KEY, CSE_ID);
//            URL url = new URL(urlString);
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("GET");
//
//            int status = connection.getResponseCode();
//            BufferedReader reader;
//            if (status > 299) {
//                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
//            } else {
//                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            }
//
//            StringBuilder response = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                response.append(line);
//            }
//            reader.close();
//
//            return response.toString();
//        } catch (IOException e) {
//            Log.e("ProductSearch", "Error during Custom Search API call", e);
//            return null;
//        }
//    }
//
//    @Override
//    protected String doInBackground(List<String>... params) {
//        //List<String> labels = (List<String>) params[0];
//        List<String> labels = new ArrayList<>();
//        labels.add("t-shirt");
//        labels.add("red");
//        labels.add("print");
//        double latitude = 44.4268;
//        double longitude = 26.1025;
//
//        String placesResponse = getNearbyStores(latitude, longitude);
//        if (placesResponse == null) {
//            return null;
//        }
//
//        StringBuilder refinedQuery = new StringBuilder();
//        for (String label : labels) {
//            refinedQuery.append(label).append(" ");
//        }
//        refinedQuery.append("item ");
//        refinedQuery.append("buy OR store OR shopping OR shop OR magazin OR cumpara OR sale");
//
//        List<String> storeNames = extractStoreNamesFromPlacesResponse(placesResponse);
//        for (String storeName : storeNames) {
//            Log.e("Store", storeName);
//            refinedQuery.append(storeName).append(" OR ");
//        }
//
//        return searchProductsAtStores(refinedQuery.toString());
//    }
//
//    @Override
//    protected void onPostExecute(String result) {
//        if (result != null) {
//            Log.d("ProductSearch", "Response: " + result);
//            parseProductSearchResponse(result);
//        } else {
//            Log.e("ProductSearch", "API call failed");
//        }
//    }
//
//    private List<String> extractStoreNamesFromPlacesResponse(String placesResponse) {
//        List<String> storeNames = new ArrayList<>();
//        try {
//            JSONObject jsonResponse = new JSONObject(placesResponse);
//            JSONArray results = jsonResponse.getJSONArray("results");
//            for (int i = 0; i < results.length(); i++) {
//                JSONObject result = results.getJSONObject(i);
//                String name = result.getString("name");
//                storeNames.add(name);
//            }
//        } catch (JSONException e) {
//            Log.e("ProductSearch", "Error parsing Places API response", e);
//        }
//        return storeNames;
//    }
//
//    private void parseProductSearchResponse(String responseJson) {
//        try {
//            JSONObject jsonResponse = new JSONObject(responseJson);
//            JSONArray items = jsonResponse.getJSONArray("items");
//
//            for (int i = 0; i < items.length(); i++) {
//                JSONObject item = items.getJSONObject(i);
//                String title = item.getString("title");
//                String link = item.getString("link");
//                String snippet = item.getString("snippet");
//
//                if (link.contains("amazon") || link.contains("ebay") || link.contains("shop") || link.contains("store")
//                        || link.contains("zara") || link.contains("h&m") || link.contains("mango") || link.contains("bershka")
//                        || link.contains("magazin")) {
//                    Log.d("ProductSearch", "Product Title: " + title);
//                    Log.d("ProductSearch", "Product Link: " + link);
//                    Log.d("ProductSearch", "Snippet: " + snippet);
//                }
//            }
//        } catch (JSONException e) {
//            Log.e("ProductSearch", "Error parsing the response", e);
//        }
//    }
//}
