package com.example.dressfind.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.example.dressfind.R;
import com.example.dressfind.recyclerviews.CalendarAdapter;
import com.example.dressfind.models.DayOutfits;
import com.example.dressfind.models.Outfit;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CaledarActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCalendar;
    private CalendarAdapter calendarAdapter;
    private FirebaseFirestore db;
    private List<DayOutfits> calendarDays = new ArrayList<>();
    BottomNavigationView bottomNavigationView ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caledar);

        db = FirebaseFirestore.getInstance();
        recyclerViewCalendar = findViewById(R.id.recyclerViewCalendar);
        recyclerViewCalendar.setLayoutManager(new LinearLayoutManager(this));
        calendarAdapter = new CalendarAdapter(CaledarActivity.this,calendarDays);
        recyclerViewCalendar.setAdapter(calendarAdapter);

        bottomNavigationView = findViewById(R.id.includeNavBar);

        bottomNavigationView.setSelectedItemId(R.id.nav_outfits);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_wardrobe) {
                Intent scanIntent = new Intent(CaledarActivity.this, MyWardrobeActivity.class);
                startActivity(scanIntent);
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_scan) {
                Intent scanIntent = new Intent(CaledarActivity.this, MainActivity.class);
                startActivity(scanIntent);
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_home) {
                Intent scanIntent = new Intent(CaledarActivity.this, ExploreActivity.class);
                startActivity(scanIntent);
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_outfits) {
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                Intent profileIntent = new Intent(CaledarActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
                return true;
            }else return true;
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        fetchOutfitsForCalendar();
    }
    private void fetchOutfitsForCalendar() {
        db.collection("outfits")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, List<Outfit>> dayToOutfitsMap = new HashMap<>();
                        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
                        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Outfit outfit = document.toObject(Outfit.class);
                            outfit.setOutfitId(document.getId());

                            if (outfit.getScheduledDate() != null) {
                                String dayKey = dayFormat.format(outfit.getScheduledDate());
                                dayToOutfitsMap.putIfAbsent(dayKey, new ArrayList<>());
                                dayToOutfitsMap.get(dayKey).add(outfit);
                            }
                        }
                        calendarDays.clear();
                        for (Map.Entry<String, List<Outfit>> entry : dayToOutfitsMap.entrySet()) {
                            calendarDays.add(new DayOutfits(entry.getKey(), entry.getValue()));
                        }

                        calendarAdapter.notifyDataSetChanged();
                    }
                });
    }
}