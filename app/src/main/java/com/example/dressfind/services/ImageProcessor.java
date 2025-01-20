package com.example.dressfind.services;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.*;

public class ImageProcessor {

    private static final String API_KEY = "9Q1ez9Z5enR2eQNWdGEufiEP";
    private static final String BASE_URL = "https://api.remove.bg/v1.0/removebg";
    private static FirebaseStorage firebaseStorage;

    public ImageProcessor() {
        firebaseStorage = FirebaseStorage.getInstance();
    }

    public void removeBackgroundAsync(Uri imageUri, OnBackgroundRemovedListener listener) {
        new RemoveBackgroundTask(imageUri, listener).execute();
    }

    private static class RemoveBackgroundTask extends AsyncTask<Void, Void, Bitmap> {

        private Uri imageUri;
        private OnBackgroundRemovedListener listener;

        public RemoveBackgroundTask(Uri imageUri, OnBackgroundRemovedListener listener) {
            this.imageUri = imageUri;
            this.listener = listener;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            File imageFile = new File(imageUri.getPath());

            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image_file", imageFile.getName(),
                            RequestBody.create(imageFile, MediaType.parse("image/jpeg")))
                    .addFormDataPart("size", "auto")
                    .addFormDataPart("format", "jpg");

            RequestBody requestBody = builder.build();

            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .post(requestBody)
                    .addHeader("X-Api-Key", API_KEY)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                Log.i("response status: ", String.valueOf(response.code()));
                if (response.isSuccessful()) {
                    InputStream inputStream = response.body().byteStream();
                    return BitmapFactory.decodeStream(inputStream);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (result != null) {
                // Save the processed bitmap to a file
                File savedFile = saveBitmapToFile(result, "IMG_bg.jpg");
                if (savedFile != null) {
                    Log.d("ImageProcessor", "Processed image saved locally: " + savedFile.getAbsolutePath());
                } else {
                    Log.e("ImageProcessor", "Failed to save processed image locally.");
                }

                // Upload the processed image to Firebase
                uploadToFirebase(result);

                // Notify listener with the result
                if (listener != null) {
                    listener.onBackgroundRemoved(result);
                }
            } else {
                Log.e("ImageProcessor", "Background removal failed, result is null.");
                if (listener != null) {
                    listener.onBackgroundRemoved(null);
                }
            }
        }

        private File saveBitmapToFile(Bitmap bitmap, String fileName) {
            File file = new File(firebaseStorage.getApp().getApplicationContext().getCacheDir(), fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                return file;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public interface OnBackgroundRemovedListener {
        void onBackgroundRemoved(Bitmap bitmap);
    }

    public static void uploadToFirebase(Bitmap photo) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        String fileName = "images/" + "IMG_bg" + ".jpg";

        StorageReference storageReference = firebaseStorage.getReference().child(fileName);

        UploadTask uploadTask = storageReference.putBytes(imageData);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                Log.d("FirebaseStorage", "Image uploaded: " + uri.toString());
            });
        }).addOnFailureListener(e -> {
            Log.e("FirebaseStorage", "Image upload failed", e);
        });
    }
}
