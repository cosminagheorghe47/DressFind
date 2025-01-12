package com.example.dressfind.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dressfind.R;
import com.example.dressfind.models.Outfit;
import com.example.dressfind.models.WardrobeItem;
import com.example.dressfind.recyclerviews.OutfitsAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyOutfitsActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView ;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private RecyclerView recyclerViewOutfits;
    private OutfitsAdapter outfitsAdapter;
    private Button button_generate_outfit;


    private final List<Outfit> outfits = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_outfits);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerViewOutfits = findViewById(R.id.recyclerView_outfits);
        recyclerViewOutfits.setLayoutManager(new GridLayoutManager(this, 2));
        outfitsAdapter = new OutfitsAdapter(this, outfits);
        recyclerViewOutfits.setAdapter(outfitsAdapter);

        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class); // Redirecționează către ecranul de autentificare
            startActivity(intent);
            finish();
            return;
        }
        else{
            fetchOutfits();
        }

        button_generate_outfit = findViewById(R.id.button_generate_outfit);

        button_generate_outfit.setOnClickListener(v -> {
            Intent intent = new Intent(MyOutfitsActivity.this, GenerateOutfitActivity.class);
            startActivity(intent);
        });


        bottomNavigationView = findViewById(R.id.includeNavBar);

        bottomNavigationView.setSelectedItemId(R.id.nav_outfits);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_outfits) {
                return true;
            } else if (item.getItemId() == R.id.nav_scan) {
                Intent scanIntent = new Intent(MyOutfitsActivity.this, MainActivity.class);
                startActivity(scanIntent);
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_home) {
                Intent scanIntent = new Intent(MyOutfitsActivity.this, HomeActivity.class);
                startActivity(scanIntent);
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_wardrobe) {
                Intent scanIntent = new Intent(MyOutfitsActivity.this, MyWardrobeActivity.class);
                startActivity(scanIntent);
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                Intent profileIntent = new Intent(MyOutfitsActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
                return true;
            }else return true;
        });

    }

    //TODO: fetch my currentUser
    private void fetchOutfits() {

        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (currentUserId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("outfits")
                //.whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        outfits.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Outfit outfit = document.toObject(Outfit.class);
                            outfits.add(outfit);
                        }
                        outfitsAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("Outfits", "Failed to fetch outfits: " + task.getException());
                        Toast.makeText(this, "Failed to fetch outfits", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_outfits);
    }
}