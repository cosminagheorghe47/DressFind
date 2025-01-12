package com.example.dressfind.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.dressfind.R;
import com.example.dressfind.adapters.MatchingImageAdapter;
import com.example.dressfind.models.MatchingImage;
import com.example.dressfind.models.ScannedImage;
import com.example.dressfind.services.AppContext;
import com.example.dressfind.services.CameraService;
import com.example.dressfind.services.ImageAnalyzer;
import com.example.dressfind.services.ImageUploader;
import com.example.dressfind.services.ProductSearchTask;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
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



        showPopup();
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
            popupDialog.dismiss();
        });

        btnAddWardrobe.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Add to Wardrobe action", Toast.LENGTH_SHORT).show();
            popupDialog.dismiss();
        });

        popupDialog.show();
    }

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");

                    if (photo != null) {
                        uploadToFirebase(photo);
                        capturedImage.setImageBitmap(photo);
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

                ScannedImage scannedImage = new ScannedImage(scanId, userId, imageUrl, scanDate);

                db.collection("scannedImages")
                        .document(scanId)
                        .set(scannedImage)
                        .addOnSuccessListener(aVoid -> Log.d("Firestore", "ScannedImage added successfully!"))
                        .addOnFailureListener(e -> Log.e("Firestore", "Failed to add ScannedImage", e));

                ImageUploader imageUploader = new ImageUploader();
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
            });
        }).addOnFailureListener(e -> {
            Log.e("FirebaseStorage", "Image upload failed", e);
            Toast.makeText(this, "Image upload failed!", Toast.LENGTH_SHORT).show();
        });
    }

}