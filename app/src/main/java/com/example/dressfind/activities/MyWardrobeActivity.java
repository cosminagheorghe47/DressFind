package com.example.dressfind.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.dressfind.models.CreatePinResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import android.content.ActivityNotFoundException;
import com.example.dressfind.R;
import com.example.dressfind.models.WardrobeItem;
import com.example.dressfind.recyclerviews.CategoryAdapter;
import com.example.dressfind.recyclerviews.WardrobeItemAdapter;
import com.example.dressfind.services.PinterestService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.Base64;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyWardrobeActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView ;

    private RecyclerView recyclerViewCategories;
    private RecyclerView recyclerViewClothes;
    private CategoryAdapter categoryAdapter;
    private WardrobeItemAdapter wardrobeItemAdapter;

    private final List<String> categories = Arrays.asList("T-Shirts", "Shirts", "Pullovers", "Pants", "Dresses", "Coats", "Sneakers", "Sandals", "Boots", "Bags");
    private final List<WardrobeItem> wardrobeItems = new ArrayList<>();
    private static final String TAG = "PinterestAPI";
    private Button button_generate_outfit;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wardrobe);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerViewCategories = findViewById(R.id.recyclerView_categories);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(this, categories, category -> fetchWardrobeItemsByCategory(category));
        recyclerViewCategories.setAdapter(categoryAdapter);

        recyclerViewClothes = findViewById(R.id.recyclerView_clothes);
        recyclerViewClothes.setLayoutManager(new GridLayoutManager(this, 2));
        wardrobeItemAdapter = new WardrobeItemAdapter(this, wardrobeItems, new WardrobeItemAdapter.OnCreatePinClickListener() {
            @Override
            public void onCreatePinClick(WardrobeItem item, String description) {
//                fetchPublicImageUrl(item.getImage(), new FirebaseUrlCallback() {
//                    @Override
//                    public void onSuccess(String publicUrl) {
                Log.e(TAG, "PUBLIC URL : " + item.getImage());
                PinterestService pinterestService = new PinterestService(MyWardrobeActivity.this);

                pinterestService.createPin(
                        "967359263650636536",
                        item.getImage(),
                        item.getName(),
                        description,
                        new PinterestService.CreatePinCallback() {
                            @Override
                            public void onSuccess(CreatePinResponse createPinResponse) {
                                Log.d(TAG, "Pin created successfully: " + createPinResponse);
                                String boardUrl = "https://ro.pinterest.com/DressFindAccount/dressfindpins/";

                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(boardUrl));
                                intent.setPackage("com.pinterest");
                                try {
                                    startActivity(intent);
                                } catch (ActivityNotFoundException e) {
                                    // Pinterest app is not installed, fall back to the web browser
                                    String fallbackUrl = "https://www.pinterest.com/DressFindAccount/dressfindpins/";
                                    Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl));
                                    startActivity(fallbackIntent);
                                }
                            }

                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "Failed to create pin: " + error);
                            }
                        }
                );
//                    }

//                    @Override
//                    public void onError(Exception e) {
//                        Log.e(TAG, "Failed to fetch public URL: " + e.getMessage());
//                        Toast.makeText(MyWardrobeActivity.this, "Failed to fetch public URL", Toast.LENGTH_SHORT).show();
//                    }
//                });
            }
        });
        recyclerViewClothes.setAdapter(wardrobeItemAdapter);

        button_generate_outfit = findViewById(R.id.button_generate_outfit);


        button_generate_outfit.setOnClickListener(v -> {
            Intent intent = new Intent(MyWardrobeActivity.this, GenerateOutfitActivity.class);
            startActivity(intent);
        });


        loadCategories();


        bottomNavigationView = findViewById(R.id.includeNavBar);

        bottomNavigationView.setSelectedItemId(R.id.nav_wardrobe);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_wardrobe) {
                return true;
            } else if (item.getItemId() == R.id.nav_scan) {
                Intent scanIntent = new Intent(MyWardrobeActivity.this, MainActivity.class);
                startActivity(scanIntent);
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_home) {
                Intent scanIntent = new Intent(MyWardrobeActivity.this, ExploreActivity.class);
                startActivity(scanIntent);
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_outfits) {
                Intent scanIntent = new Intent(MyWardrobeActivity.this, MyOutfitsActivity.class);
                startActivity(scanIntent);
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                Intent profileIntent = new Intent(MyWardrobeActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
                return true;
            }else return true;
        });
    }

    private void loadCategories() {
        categoryAdapter.notifyDataSetChanged();

        recyclerViewCategories.post(() -> {
            categoryAdapter.notifyDataSetChanged();
            recyclerViewCategories.scrollToPosition(0);
        });

        // Selectează implicit prima categorie
        if (!categories.isEmpty()) {
            fetchWardrobeItemsByCategory(categories.get(0));
        }

    }
    //TODO: fetch by currentUser
    private void fetchWardrobeItemsByCategory(String category) {
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (currentUserId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("wardrobeItem")
                .whereEqualTo("Category", category)
                //.whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        wardrobeItems.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            WardrobeItem item = document.toObject(WardrobeItem.class);
                            wardrobeItems.add(item);
                        }
                        wardrobeItemAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("Wardrobe", "Failed to fetch items: " + task.getException());
                        Toast.makeText(this, "Failed to fetch items", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_wardrobe);
    }
}