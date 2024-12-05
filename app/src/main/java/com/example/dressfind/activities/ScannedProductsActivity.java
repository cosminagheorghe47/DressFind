package com.example.dressfind.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.dressfind.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ScannedProductsActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_products);
        bottomNavigationView = findViewById(R.id.includeNavBar);

        bottomNavigationView.setSelectedItemId(R.id.nav_scan);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_wardrobe) {
                Intent scanIntent = new Intent(ScannedProductsActivity.this, MyWardrobeActivity.class);
                startActivity(scanIntent);
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_scan) {
                Intent scanIntent = new Intent(ScannedProductsActivity.this, MainActivity.class);
                startActivity(scanIntent);
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_home) {
                Intent scanIntent = new Intent(ScannedProductsActivity.this, HomeActivity.class);
                startActivity(scanIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_outfits) {
//                Intent scanIntent = new Intent(ScannedProductsActivity.this, MainActivity.class);
//                startActivity(scanIntent);
//                overridePendingTransition(0, 0);
                return true;
            } else
                return item.getItemId() == R.id.nav_profile;

        });
    }
    @Override
    protected void onResume() {
        super.onResume();
//        bottomNavigationView.setSelectedItemId(R.id.nav_scan);
    }
}