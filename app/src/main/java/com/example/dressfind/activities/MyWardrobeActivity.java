package com.example.dressfind.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.dressfind.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MyWardrobeActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wardrobe);

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
                Intent scanIntent = new Intent(MyWardrobeActivity.this, HomeActivity.class);
                startActivity(scanIntent);
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_outfits) {
//                Intent scanIntent = new Intent(MyWardrobeActivity.this, MainActivity.class);
//                startActivity(scanIntent);
//                overridePendingTransition(0, 0);
                return true;
            } else return item.getItemId() == R.id.nav_profile;
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_wardrobe);
    }
}