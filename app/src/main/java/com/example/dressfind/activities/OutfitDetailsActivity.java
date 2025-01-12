package com.example.dressfind.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dressfind.R;
import com.example.dressfind.models.CreatePinResponse;
import com.example.dressfind.models.Outfit;
import com.example.dressfind.models.OutfitsItems;
import com.example.dressfind.models.WardrobeItem;
import com.example.dressfind.recyclerviews.WardrobeItemAdapter;
import com.example.dressfind.services.PinterestService;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OutfitDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private WardrobeItemAdapter wardrobeItemAdapter;
    private Button scheduleOutfitButton;
    private RecyclerView recyclerViewWardrobeItems;
    private static final String TAG = "PinterestAPI";
    private List<WardrobeItem> wardrobeItems = new ArrayList<>();
    private Outfit currentOutfit;
    private TextView titlePage;
    private String outfitId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_details);

        db = FirebaseFirestore.getInstance();

        scheduleOutfitButton = findViewById(R.id.scheduleOutfitButton);

        scheduleOutfitButton.setOnClickListener(v -> openDatePicker());


        recyclerViewWardrobeItems = findViewById(R.id.recyclerViewWardrobeItems);
        recyclerViewWardrobeItems.setLayoutManager(new GridLayoutManager(this, 2));
        wardrobeItemAdapter = new WardrobeItemAdapter(this, wardrobeItems, new WardrobeItemAdapter.OnCreatePinClickListener() {
            @Override
            public void onCreatePinClick(WardrobeItem item, String description) {
//                fetchPublicImageUrl(item.getImage(), new FirebaseUrlCallback() {
//                    @Override
//                    public void onSuccess(String publicUrl) {
                Log.e(TAG, "PUBLIC URL : " + item.getImage());
                PinterestService pinterestService = new PinterestService(OutfitDetailsActivity.this);

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
                                    // pinterest app is not installed, fall back to the web browser
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
        recyclerViewWardrobeItems.setAdapter(wardrobeItemAdapter);

        outfitId = getIntent().getStringExtra("outfitId");

        if (outfitId == null || outfitId.isEmpty()) {
            Toast.makeText(this, "Outfit ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        titlePage = findViewById(R.id.titlePage);
    }
    @Override
    protected void onResume() {
        super.onResume();
        fetchOutfitDetails();
    }
    private void fetchOutfitDetails() {
        if (outfitId == null || outfitId.isEmpty()) {
            Toast.makeText(this, "Outfit ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("outfits").document(outfitId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentOutfit = documentSnapshot.toObject(Outfit.class);
                        if (currentOutfit != null) {
                            currentOutfit.setOutfitId(documentSnapshot.getId());
                            updateUI();
                        }
                    } else {
                        Toast.makeText(this, "Outfit not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("OutfitDetails", "Failed to fetch outfit details", e);
                    Toast.makeText(this, "Error fetching outfit details", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI() {
        if (currentOutfit != null) {
            titlePage.setText(currentOutfit.getName());
            updateButtonWithDate(currentOutfit.getScheduledDate());
            getItemIdsForOutfit(currentOutfit.getOutfitId());
        }
    }
    private void updateButtonWithDate(Date date) {
        if (date != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm a", Locale.getDefault());
            String formattedDate = dateFormat.format(date);
            scheduleOutfitButton.setText(formattedDate);
        } else {
            scheduleOutfitButton.setText("Schedule Outfit");
        }
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
    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (DatePicker view, int year, int month, int dayOfMonth) -> openTimePicker(year, month, dayOfMonth),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void openTimePicker(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (TimePicker view, int hourOfDay, int minute) -> saveScheduledDateTime(year, month, dayOfMonth, hourOfDay, minute),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );

        timePickerDialog.show();
    }

    private void saveScheduledDateTime(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
        Calendar scheduledCalendar = Calendar.getInstance();
        scheduledCalendar.set(year, month, dayOfMonth, hourOfDay, minute);

        if (currentOutfit != null && currentOutfit.getOutfitId() != null) {
            db.collection("outfits").document(currentOutfit.getOutfitId())
                    .update("scheduledDate", scheduledCalendar.getTime())
                    .addOnSuccessListener(aVoid -> {
                        currentOutfit.setScheduledDate(scheduledCalendar.getTime());
                        updateButtonWithDate(scheduledCalendar.getTime());
                        Toast.makeText(this, "Outfit scheduled successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreError", "Failed to schedule outfit", e);
                        Toast.makeText(this, "Failed to schedule outfit", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Outfit ID is missing. Cannot schedule outfit.", Toast.LENGTH_SHORT).show();
        }
    }
}


