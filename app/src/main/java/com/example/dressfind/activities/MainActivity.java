package com.example.dressfind.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.dressfind.R;
import com.example.dressfind.services.CameraService;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private CameraService cameraService;
//    private StorageReference storageReference;
    private ImageView capturedImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        cameraService = new CameraService(this);
//        storageReference = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        ImageView homeIcon = findViewById(R.id.home_icon);
        ImageView cameraIcon = findViewById(R.id.camera_icon);
        ImageView collectionIcon = findViewById(R.id.collection_icon);
        capturedImage = findViewById(R.id.captured_image);

        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Left", Toast.LENGTH_SHORT).show();
            }
        });

        cameraIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    cameraService.openCamera(cameraLauncher);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });

        collectionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Right", Toast.LENGTH_SHORT).show();
            }
        });



        //TEST ADAUGARE WARDROBE ITEM IN DB  --> A MERS
//        WardrobeItem item = new WardrobeItem(
//                "12345",
//                "Red",
//                "A red shirt",
//                "https://example.com/image.jpg",
//                "Cotton",
//                "Shirt",
//                "scan123",
//                "user123"
//        );
    }
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");

                    if (photo != null) {
                        //Trebuie sa facem upgrade plan pe firebase pentru storage, poate salvam pozele altundeva (imgur?)
                        //uploadToFirebase(photo);

                        capturedImage.setImageBitmap(photo);
                    } else {
                        Toast.makeText(this, "Failed to capture image.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Operation canceled.", Toast.LENGTH_SHORT).show();
                }
            });


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


//    private void uploadToFirebase(Bitmap photo) {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//        byte[] imageData = baos.toByteArray();
//
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String fileName = "images/" + "IMG_" + timeStamp + ".jpg";
//
//        StorageReference imageRef = storageReference.child(fileName);
//
//        UploadTask uploadTask = imageRef.putBytes(imageData);
//        uploadTask.addOnSuccessListener(taskSnapshot -> {
//            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
//                Log.d("FirebaseStorage", "Image uploaded: " + uri.toString());
//                Toast.makeText(this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
//            });
//        }).addOnFailureListener(e -> {
//            Log.e("FirebaseStorage", "Image upload failed", e);
//            Toast.makeText(this, "Image upload failed!", Toast.LENGTH_SHORT).show();
//        });
//    }
}