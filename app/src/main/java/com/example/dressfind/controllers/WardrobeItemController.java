package com.example.dressfind.controllers;

import android.util.Log;

import com.example.dressfind.models.WardrobeItem;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import android.widget.Toast;

public class WardrobeItemController {


    private FirebaseFirestore db;

    public WardrobeItemController() {
        this.db = FirebaseFirestore.getInstance();
    }

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

    public interface FetchWardrobeItemsCallback {
        void onSuccess(List<WardrobeItem> items);
        void onFailure(Exception e);
    }

    public void fetchWardrobeItemsByCategory(String category, FetchWardrobeItemsCallback callback) {
        db.collection("wardrobeItem")
                .whereEqualTo("Category", category)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<WardrobeItem> items = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            WardrobeItem item = document.toObject(WardrobeItem.class);
                            items.add(item);
                        }
                        // Trimite lista prin callback
                        callback.onSuccess(items);
                    } else {
                        // Trimite eroarea prin callback
                        callback.onFailure(task.getException());
                        Log.v("Firestore", "Error getting list of clothes");
                    }
                });
    }




}
