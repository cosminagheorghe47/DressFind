package com.example.dressfind.services;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

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

public class ImageUploader {

    private static final String API_KEY = "AIzaSyCSS-WiCPU2UF6Znol71aIw-da6dlQfnz0";
    private static final String VISION_API_URL = "https://vision.googleapis.com/v1/images:annotate?key=" + API_KEY;
    static String imageUrl;
    private static final String TAG = "ImageUploader";
    public interface CropCallback {
        void onCroppedImageUrl(String croppedImageUrl);
        void onError(Exception e);
    }

    public void callVisionAPI(String imageUrl, CropCallback callback) {
        new VisionApiTask(callback).execute(imageUrl);
    }

    private static class VisionApiTask extends AsyncTask<String, Void, String> {

        private final CropCallback callback;

        VisionApiTask(CropCallback callback) {
            this.callback = callback;
        }

        @Override
        protected String doInBackground(String... params) {
            imageUrl = params[0];
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

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                visionResponse(result, callback);
            } else {
                callback.onError(new Exception("API call failed"));
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
    }

    private static void visionResponse(String responseJson, CropCallback callback) {
        try {
            JSONObject jsonResponse = new JSONObject(responseJson);
            JSONArray responses = jsonResponse.getJSONArray("responses");

            JSONArray localizedObjectAnnotations = responses.getJSONObject(0).optJSONArray("localizedObjectAnnotations");
            if (localizedObjectAnnotations != null && localizedObjectAnnotations.length() > 0) {
                JSONObject boundingPoly = localizedObjectAnnotations.getJSONObject(0).optJSONObject("boundingPoly");
                if (boundingPoly != null) {
                    JSONArray normalizedVertices = boundingPoly.optJSONArray("normalizedVertices");
                    if (normalizedVertices != null && normalizedVertices.length() >= 4) {
                        getImageDimensions(imageUrl, normalizedVertices, callback);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e("VisionAPI", "Error parsing the response", e);
            callback.onError(e);
        }
    }

    private static void getImageDimensions(String imageUrl, final JSONArray normalizedVertices, CropCallback callback) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl(imageUrl);

        storageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap != null) {
                int imageWidth = bitmap.getWidth();
                int imageHeight = bitmap.getHeight();
                processNormalizedValues(normalizedVertices, imageWidth, imageHeight, bitmap, callback);
            }
        }).addOnFailureListener(callback::onError);
    }

    private static void processNormalizedValues(JSONArray normalizedVertices, int imageWidth, int imageHeight, Bitmap bitmap, CropCallback callback) {
        try {
            float x1 = (float) normalizedVertices.getJSONObject(0).optDouble("x", 0.0);
            float y1 = (float) normalizedVertices.getJSONObject(0).optDouble("y", 0.0);
            float x2 = (float) normalizedVertices.getJSONObject(2).optDouble("x", 1.0);
            float y2 = (float) normalizedVertices.getJSONObject(2).optDouble("y", 1.0);

            int px1 = Math.round(x1 * imageWidth);
            int py1 = Math.round(y1 * imageHeight);
            int px2 = Math.round(x2 * imageWidth);
            int py2 = Math.round(y2 * imageHeight);

            cropImage(bitmap, px1, py1, px2, py2, callback);
        } catch (JSONException e) {
            Log.e("VisionAPI", "Error processing normalized bounding box", e);
            callback.onError(e);
        }
    }

    private static void cropImage(Bitmap bitmap, int x1, int y1, int x2, int y2, CropCallback callback) {
        try {
            Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, x1, y1, x2 - x1, y2 - y1);
            uploadCroppedImageToStorage(croppedBitmap, callback);
        } catch (Exception e) {
            Log.e("VisionAPI", "Error cropping the image", e);
            callback.onError(e);
        }
    }

    private static void uploadCroppedImageToStorage(Bitmap croppedBitmap, CropCallback callback) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            String fileName = "cropped_image.jpg";
            StorageReference croppedImageRef = storageRef.child("cropped_images/" + fileName);

            croppedImageRef.putBytes(data).addOnSuccessListener(taskSnapshot -> {
                croppedImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String croppedImageUrl = uri.toString();
                    callback.onCroppedImageUrl(croppedImageUrl);
                });
            }).addOnFailureListener(callback::onError);
        } catch (Exception e) {
            Log.e("VisionAPI", "Error uploading cropped image", e);
            callback.onError(e);
        }
    }
    public interface ResizeCallback {
        void onResized(String resizedImageUrl);

        default void onError(String error) {

        }
    }
        public static void resizeImage(String imageUrl, int minDimension, int maxDimension, ResizeCallback callback) {
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                Picasso.get().load(imageUrl).into(new com.squareup.picasso.Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        int width = bitmap.getWidth();
                        int height = bitmap.getHeight();

                        // Determine the scale factor to resize within the allowed range
                        float scale = Math.min(Math.max((float) minDimension / Math.min(width, height), 1.0f),
                                (float) maxDimension / Math.max(width, height));

                        Matrix matrix = new Matrix();
                        matrix.postScale(scale, scale);

                        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                        uploadToFirebase(resizedBitmap, callback);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, android.graphics.drawable.Drawable errorDrawable) {
                        Log.e(TAG, "Failed to load image: " + e.getMessage());
                        callback.onError("Failed to load image.");
                    }

                    @Override
                    public void onPrepareLoad(android.graphics.drawable.Drawable placeHolderDrawable) {
                        // Optional
                    }
                });
            });
        }

    private static void uploadToFirebase(Bitmap bitmap, ResizeCallback callback) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("resized_images/" + System.currentTimeMillis() + ".jpg");

        storageRef.putBytes(imageData).addOnSuccessListener(taskSnapshot ->
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d(TAG, "Resized image uploaded: " + uri.toString());
                    callback.onResized(uri.toString());
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get download URL: " + e.getMessage());
                    callback.onError("Failed to get download URL.");
                })
        ).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to upload resized image: " + e.getMessage());
            callback.onError("Failed to upload resized image.");
        });
    }
    public static void checkImageSize(String imageUrl, ImageSizeCallback callback, ResizeCallback errorCallback) {
        Picasso.get().load(imageUrl).into(new com.squareup.picasso.Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                callback.onSizeRetrieved(width, height);
            }

            @Override
            public void onBitmapFailed(Exception e, android.graphics.drawable.Drawable errorDrawable) {
                Log.e(TAG, "Failed to load image for size check: " + e.getMessage());
                errorCallback.onError("Failed to load image for size check.");
            }

            @Override
            public void onPrepareLoad(android.graphics.drawable.Drawable placeHolderDrawable) {
                // Optional
            }
        });
    }

    public interface ImageSizeCallback {
        void onSizeRetrieved(int width, int height);
    }

}
