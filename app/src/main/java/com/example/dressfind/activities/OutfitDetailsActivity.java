package com.example.dressfind.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dressfind.R;
import com.example.dressfind.models.Outfit;
import com.example.dressfind.models.OutfitsItems;
import com.example.dressfind.models.WardrobeItem;
import com.example.dressfind.recyclerviews.WardrobeItemAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OutfitDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private WardrobeItemAdapter wardrobeItemAdapter;
    private RecyclerView recyclerViewWardrobeItems;
    private List<WardrobeItem> wardrobeItems = new ArrayList<>();
    private TextView titlePage;
    private Button publishButton;
    BottomNavigationView bottomNavigationView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_details);

        db = FirebaseFirestore.getInstance();

        recyclerViewWardrobeItems = findViewById(R.id.recyclerViewWardrobeItems);
        recyclerViewWardrobeItems.setLayoutManager(new GridLayoutManager(this, 2));
        wardrobeItemAdapter = new WardrobeItemAdapter(this, wardrobeItems);
        recyclerViewWardrobeItems.setAdapter(wardrobeItemAdapter);

        Intent intent = getIntent();
        Outfit outfit = intent.getParcelableExtra("currentOutfit");
        Log.i("OUTFIT FROM INTENT: ", String.valueOf(outfit));

        titlePage = findViewById(R.id.titlePage);
        titlePage.setText(outfit.getName());

        publishButton = findViewById(R.id.button_publish_outfit);  // Find the button by ID

        setPublishButtonText(outfit);

        bottomNavigationView = findViewById(R.id.includeNavBar);
        bottomNavigationView.setSelectedItemId(R.id.nav_outfits);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_outfits) {
                return true;
            } else if (item.getItemId() == R.id.nav_scan) {
                Intent scanIntent = new Intent(OutfitDetailsActivity.this, MainActivity.class);
                startActivity(scanIntent);
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_home) {
                Intent scanIntent = new Intent(OutfitDetailsActivity.this, HomeActivity.class);
                startActivity(scanIntent);
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_wardrobe) {
                Intent scanIntent = new Intent(OutfitDetailsActivity.this, MyWardrobeActivity.class);
                startActivity(scanIntent);
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                Intent profileIntent = new Intent(OutfitDetailsActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
                return true;
            } else return true;
        });

        Log.i("current outfit: ", String.valueOf(outfit));
        Log.i("current outfit id: ", outfit.getOutfitId());

        if(outfit.getOutfitId() != null)
            getItemIdsForOutfit(outfit.getOutfitId());

        publishButton.setOnClickListener(v -> {
            togglePublishStatus(outfit);
        });
    }

    private void setPublishButtonText(Outfit outfit) {
        Log.i("current OUTFIT: ", String.valueOf(outfit));
        if (outfit.isPublic()) {
            publishButton.setText("Delete Publish");
        } else {
            publishButton.setText("Publish Outfit");
        }
    }

    private void togglePublishStatus(Outfit outfit) {
        boolean newPublishStatus = !outfit.isPublic();
        outfit.setPublic(newPublishStatus);

        setPublishButtonText(outfit);

        db.collection("outfits")
                .document(outfit.getOutfitId())
                .update("isPublic", newPublishStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(OutfitDetailsActivity.this, "Outfit publish status updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(OutfitDetailsActivity.this, "Failed to update publish status", Toast.LENGTH_SHORT).show();
                });
    }

    public void getItemIdsForOutfit(String outfitId) {
        Log.i("entered get items: ", outfitId);

        db.collection("outfitsItems")
                .whereEqualTo("outfitId", outfitId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.i("found items ig: ", String.valueOf(task.getResult().size()));
                        wardrobeItems.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.i("doc: ", String.valueOf(document));
                            OutfitsItems outfitsItems = document.toObject(OutfitsItems.class);

                            getWardrobeItemDetails(outfitsItems.getItemId());
                        }

                    } else {
                        Log.e("OutfitItems", "Failed to fetch outfit items: " + task.getException());
                        Toast.makeText(this, "Failed to fetch items", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getWardrobeItemDetails(String itemId) {
        Log.i("item id: ", itemId);

        db.collection("wardrobeItem")
                .whereEqualTo("itemId", itemId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.i("document: ", document.getId() + " => " + document.getData());

                            if (document.exists()) {
                                WardrobeItem wardrobeItem = document.toObject(WardrobeItem.class);
                                Log.i("WARDROBE ITEM: ", String.valueOf(wardrobeItem));

                                if (wardrobeItem != null) {
                                    wardrobeItems.add(wardrobeItem);
                                    wardrobeItemAdapter.notifyDataSetChanged();
                                }
                            } else {
                                Log.e("WardrobeItem", "No such document with itemId: " + itemId);
                            }
                        }
                    } else {
                        Log.e("WardrobeItem", "Failed to fetch wardrobe item details: " + task.getException());
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_outfits);
    }
}
