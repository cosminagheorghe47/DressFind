package com.example.dressfind.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.dressfind.R;
import com.example.dressfind.models.PredictionResponse;
import com.example.dressfind.adapters.MatchingImageAdapter;
import com.example.dressfind.models.MatchingImage;
import com.example.dressfind.models.ScannedImage;
import com.example.dressfind.services.AppContext;
import com.example.dressfind.services.CameraService;
import com.example.dressfind.services.ImageAnalyzer;
import com.example.dressfind.services.ImageProcessor;
import com.example.dressfind.services.ImageUploader;
import com.example.dressfind.services.ProductSearchTask;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity implements ProductSearchTask.ProductSearchCallback {
    private FirebaseFirestore db;
    private CameraService cameraService;
    private StorageReference storageReference;
    private ImageView capturedImage;
    private ImageAnalyzer imageAnalyzer;
    private RecyclerView recyclerView;
    private MatchingImageAdapter matchingImageAdapter;
    private List<MatchingImage> matchingImages = new ArrayList<>();
    private ProductSearchTask productSearchTask;
    private FirebaseAuth auth;
    private Boolean AiButton = false;

    private boolean googleSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        cameraService = new CameraService(this);
        storageReference = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        capturedImage = findViewById(R.id.captured_image);

        AppContext.setContext(this);

        auth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        matchingImageAdapter = new MatchingImageAdapter(this);
        recyclerView.setAdapter(matchingImageAdapter);


        productSearchTask = new ProductSearchTask();
        productSearchTask.setCallback(this);
        Intent intent = getIntent();
        boolean fromHistory = intent.getBooleanExtra("fromHistory", false);

        if (fromHistory) {
            String imageUrl = intent.getStringExtra("imageUrl");
            if (imageUrl != null) {
                fetchBitmapFromUrl(imageUrl, (bitmap, e) -> {
                    if (e == null) {
                        uploadToFirebase(bitmap);
                        capturedImage.setImageBitmap(bitmap);
                    } else {
                        Log.e("MainActivity", "Error fetching bitmap", e);
                    }
                });
            }
        } else {
            showPopup();
        }

    }
    private void fetchBitmapFromUrl(String imageUrl, BitmapCallback callback) {
        new Thread(() -> {
            try {
                Bitmap bitmap = Picasso.get().load(imageUrl).get();
                runOnUiThread(() -> callback.onResult(bitmap, null));
            } catch (Exception e) {
                runOnUiThread(() -> callback.onResult(null, e));
            }
        }).start();
    }

    interface BitmapCallback {
        void onResult(Bitmap bitmap, Exception e);
    }
    private void showPopup() {
        Dialog popupDialog = new Dialog(this);
        popupDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        popupDialog.setContentView(R.layout.popup_layout);
        popupDialog.setCancelable(true);

        Window window = popupDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(layoutParams);
        }

        Button btnSearchGoogle = popupDialog.findViewById(R.id.btn_search_google);
        Button btnAddWardrobe = popupDialog.findViewById(R.id.btn_add_wardrobe);

        btnSearchGoogle.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                cameraService.openCamera(cameraLauncher);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CAMERA}, 100);
            }
            googleSearch=true;
            popupDialog.dismiss();
        });

        btnAddWardrobe.setOnClickListener(v -> {
            AiButton = true;
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                AiButton = true;
                cameraService.openCamera(cameraLauncher);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CAMERA}, 100);
            }
            Toast.makeText(MainActivity.this, "Add to Wardrobe action", Toast.LENGTH_SHORT).show();
            googleSearch = false;
            popupDialog.dismiss();
        });

        popupDialog.show();
    }

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");

                    if (photo != null) {
                        Uri sourceUri = saveBitmapToCache(photo);
                        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped_image.jpg"));

                        UCrop.of(sourceUri, destinationUri)
                                .withAspectRatio(1, 1) // Raport de aspect pătrat (modificabil)
                                .withMaxResultSize(500, 500) // Dimensiunea maximă (modificabilă)
                                .start(MainActivity.this);
//                        uploadToFirebase(photo);
//                        capturedImage.setImageBitmap(photo);
                    } else {
                        Toast.makeText(this, "Failed to capture image.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Operation canceled.", Toast.LENGTH_SHORT).show();
                }
            });


    @Override
    public void onMatchingImagesFound(List<MatchingImage> matchingImages) {
        this.matchingImages.clear();
        this.matchingImages.addAll(matchingImages);
        matchingImageAdapter.notifyDataSetChanged();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            Uri croppedImageUri = UCrop.getOutput(data);

            if (croppedImageUri != null) {
                Bitmap croppedBitmap = getBitmapFromUri(croppedImageUri);

                if(googleSearch){
                    uploadToFirebase(croppedBitmap);
                    // Afișează imaginea decupată în ImageView
                    capturedImage.setImageURI(croppedImageUri);
                } else {
                    // delete background
                    ImageProcessor imageProcessor = new ImageProcessor();
                    imageProcessor.removeBackgroundAsync(croppedImageUri, bitmap -> {
                        if (bitmap != null) {
                            capturedImage.setImageBitmap(bitmap);
                            File noBgFile = saveBitmapToFile(bitmap, "IMG_bg.jpg");
                            if (noBgFile.exists()) {
                                Log.d("MainActivity", "Sending the file to the server: " + noBgFile.getAbsolutePath());
                                sendImageToServer(noBgFile);
                            } else {
                                Log.e("MainActivity", "Failed to save the processed image without background.");
                                Toast.makeText(this, "Error: Processed image not found.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("onBackgroundRemoved", "Failed to remove background");
                            Log.e("MainActivity", "Failed to process image without background.");
                            Toast.makeText(this, "Error processing image.", Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            if (cropError != null) {
                Log.e("MainActivity", "Crop error: ", cropError);
            }
        }
    }
    private Uri saveBitmapToCache(Bitmap bitmap) {
        File cacheFile = new File(getCacheDir(), "temp_image.jpg");
        try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Uri.fromFile(cacheFile);
    }
    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            return MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraService.openCamera(cameraLauncher);
            } else {
                Toast.makeText(this, "Camera permission is required to take a picture", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public interface ApiService {
        @Multipart
        @POST("/predict")
        Call<PredictionResponse> uploadImage(@Part MultipartBody.Part file);
    }

    public void sendImageToServer(File file) {
        // Configurăm Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dressfindml-118142837315.europe-central2.run.app")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // Convertim imaginea într-un RequestBody
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);

        // Creăm un MultipartBody.Part pentru Retrofit
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        // Facem cererea
        Call<PredictionResponse> call = apiService.uploadImage(body);
        call.enqueue(new Callback<PredictionResponse>() {
            @Override
            public void onResponse(Call<PredictionResponse> call, Response<PredictionResponse> response) {
                Log.d("Request", file.getName());
                Log.d("Response", response.message());
                if (response.isSuccessful() && response.body() != null) {
                    String result = "Predicted item: " + response.body().getClassName() +
                            " with a probability of " + response.body().getProbability();
                    Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
                } else {
                    int statusCode = response.code(); // Get HTTP status code
                    Log.e("HTTP Error", "Status Code: " + statusCode);
                    Toast.makeText(getApplicationContext(), "Preprocessing Error", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PredictionResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private File saveBitmapToFile(Bitmap bitmap, String fileName) {
        File file = new File(getCacheDir(), fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            Log.d("MainActivity", "File saved successfully: " + file.getAbsolutePath());
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("MainActivity", "Error saving bitmap to file: " + e.getMessage());
            return null;
        }
    }

    public void uploadToFirebase(Bitmap photo) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "images/" + "IMG_" + timeStamp + ".jpg";

        StorageReference imageRef = storageReference.child(fileName);

        UploadTask uploadTask = imageRef.putBytes(imageData);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Log.d("FirebaseStorage", "Image uploaded: " + uri.toString());
                Toast.makeText(this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();

                String scanId = db.collection("scannedImages").document().getId();
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String imageUrl = uri.toString();
                Date scanDate = new Date();

                Intent intent = getIntent();
                boolean fromHistory = intent.getBooleanExtra("fromHistory", false);

                if (!fromHistory) {
                    ScannedImage scannedImage = new ScannedImage(scanId, userId, imageUrl, scanDate);

                    db.collection("scannedImages")
                            .document(scanId)
                            .set(scannedImage)
                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "ScannedImage added successfully!"))
                            .addOnFailureListener(e -> Log.e("Firestore", "Failed to add ScannedImage", e));

                }

                ImageUploader imageUploader = new ImageUploader();
                if(!AiButton){
                    imageUploader.callVisionAPI(uri.toString(), new ImageUploader.CropCallback() {
                        @Override
                        public void onCroppedImageUrl(String croppedImageUrl) {
                            ImageAnalyzer imageAnalyzer = new ImageAnalyzer();
                            imageAnalyzer.setCallback(new ImageAnalyzer.ProductSearchCallback() {
                                @Override
                                public void onMatchingImagesFound(List<MatchingImage> matchingImages) {
                                    matchingImageAdapter.updateData(matchingImages);
                                    matchingImageAdapter.notifyDataSetChanged();
                                }
                            });

                            imageAnalyzer.execute(croppedImageUrl);
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("MainActivity", "Error processing image", e);
                        }
                    });
                } else{
                }
            });
        }).addOnFailureListener(e -> {
            Log.e("FirebaseStorage", "Image upload failed", e);
            Toast.makeText(this, "Image upload failed!", Toast.LENGTH_SHORT).show();
        });
    }

}