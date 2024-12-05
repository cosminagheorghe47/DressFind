package com.example.dressfind.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.dressfind.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView ;
    CardView scannedProductsCard;
    CardView MyWardrobeCard;
    CardView MyOutfitsCard;
    CardView ScanProductCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.includeNavBar);

        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                return true;
            } else if (item.getItemId() == R.id.nav_outfits) {
                return true;
            } else if (item.getItemId() == R.id.nav_scan) {
                Intent scanIntent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(scanIntent);
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_wardrobe) {
                Intent scanIntent = new Intent(HomeActivity.this, MyWardrobeActivity.class);
                startActivity(scanIntent);
                overridePendingTransition(0, 0);
                return true;
            } else return item.getItemId() == R.id.nav_profile;
        });



        scannedProductsCard=findViewById(R.id.scannedProductsCard);
        scannedProductsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent scanIntent = new Intent(HomeActivity.this, ScannedProductsActivity.class);
                startActivity(scanIntent);
            }
        });

        MyWardrobeCard=findViewById(R.id.myWardrobeCard);
        MyWardrobeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent scanIntent = new Intent(HomeActivity.this, MyWardrobeActivity.class);
                startActivity(scanIntent);
            }
        });

        ScanProductCard=findViewById(R.id.scanCard);
        ScanProductCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent scanIntent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(scanIntent);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }
}