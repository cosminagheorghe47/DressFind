package com.example.dressfind.controllers;

import android.util.Log;

import com.example.dressfind.models.WardrobeItem;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class WardrobeItemController {
    private FirebaseFirestore db;
    public void addWardribeItem(WardrobeItem wardrobeItem){
        db = FirebaseFirestore.getInstance();
        CollectionReference wardrobeItemsRef = db.collection("wardrobeItem");
        wardrobeItemsRef.add(wardrobeItem)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error adding document", e);
                });
    }
}
