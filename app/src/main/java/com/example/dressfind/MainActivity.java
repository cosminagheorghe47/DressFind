package com.example.dressfind;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        ImageView leftIcon = findViewById(R.id.left_icon);
        ImageView cameraIcon = findViewById(R.id.camera_icon);
        ImageView rightIcon = findViewById(R.id.right_icon);

        leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Left", Toast.LENGTH_SHORT).show();
            }
        });

        cameraIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Camera", Toast.LENGTH_SHORT).show();
            }
        });

        rightIcon.setOnClickListener(new View.OnClickListener() {
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
//
//        CollectionReference wardrobeItemsRef = db.collection("wardrobeItem");
//        wardrobeItemsRef.add(item)
//                .addOnSuccessListener(documentReference -> {
//                    Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference.getId());
//                })
//                .addOnFailureListener(e -> {
//                    Log.w("Firestore", "Error adding document", e);
//                });
    }
}