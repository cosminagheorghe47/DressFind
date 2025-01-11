package com.example.dressfind.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dressfind.R;
import com.example.dressfind.models.Outfit;
import com.example.dressfind.models.OutfitsItems;
import com.example.dressfind.models.WardrobeItem;
import com.example.dressfind.recyclerviews.CategoryAdapter;
import com.example.dressfind.recyclerviews.DragTouchListener;
import com.example.dressfind.recyclerviews.SmallWardrobeItemAdapter;
import com.example.dressfind.recyclerviews.WardrobeItemAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class GenerateOutfitActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCategories;
    private RecyclerView recyclerViewClothes;
    private CategoryAdapter categoryAdapter;
    private SmallWardrobeItemAdapter smallWardrobeItemAdapter;

    private ImageView generate_button;
    private ImageView cancel_button;
    private EditText editText_title;
    private FrameLayout canvasOutfit;
    private RelativeLayout edit_button;

    private final List<String> categories = Arrays.asList("T-Shirts", "Shirts", "Pullovers", "Pants", "Dresses", "Coats", "Sneakers", "Sandals", "Boots", "Bags");
    private final List<WardrobeItem> wardrobeItems = new ArrayList<>();

    private final List<WardrobeItem> selectedItemsOnCanvas = new ArrayList<>();

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_outfit);
        db = FirebaseFirestore.getInstance();

        recyclerViewCategories = findViewById(R.id.recyclerView_categories);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(this, categories, category -> fetchWardrobeItemsByCategory(category));
        recyclerViewCategories.setAdapter(categoryAdapter);

        recyclerViewClothes = findViewById(R.id.recyclerView_clothes);
        recyclerViewClothes.setLayoutManager(new GridLayoutManager(this, 3));
        smallWardrobeItemAdapter = new SmallWardrobeItemAdapter(this, wardrobeItems, this::addItemToCanvas);
        recyclerViewClothes.setAdapter(smallWardrobeItemAdapter);

        generate_button = findViewById(R.id.generate_button);
        cancel_button = findViewById(R.id.cancel_button);
        canvasOutfit = findViewById(R.id.canvas_outfit);
        editText_title = findViewById(R.id.editText_title);
        edit_button = findViewById(R.id.title);

        loadCategories();

        cancel_button.setOnClickListener(v -> {
            Intent intent = new Intent(GenerateOutfitActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        edit_button.setOnClickListener(v -> {
            editText_title.setFocusableInTouchMode(true);
            editText_title.setCursorVisible(true);
            editText_title.requestFocus();
        });

        editText_title.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                editText_title.setFocusable(false);
                editText_title.setCursorVisible(false);
            }
        });



        generate_button.setOnClickListener(v -> {
            String outfitName = editText_title.getText().toString();

            // Creăm un popup
            new AlertDialog.Builder(GenerateOutfitActivity.this)
                    .setMessage("Do you want to save the outfit: " + outfitName + "?")
                    .setPositiveButton("Yes", (dialog, which) -> saveOutfit(outfitName))
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .create().show();
        });


    }

    private void saveOutfit(String outfitName) {
        // Obținem data curentă
        Date currentDate = new Date();

        // Creăm obiectul Outfit
        Outfit outfit = new Outfit();
        outfit.setUserId("currentUserId"); // Înlocuiește cu ID-ul real al utilizatorului
        outfit.setName(outfitName);
        outfit.setCreationDate(currentDate);
        outfit.setScheduledDate(currentDate);
        outfit.setDescription("");

        // Salvăm Outfit în Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("outfits").add(outfit)
                .addOnSuccessListener(documentReference -> {
                    String outfitId = documentReference.getId();
                    saveOutfitItems(outfitId);
                    captureCanvasImage(outfitId); // Capturează imaginea canvasului
                })
                .addOnFailureListener(e -> Toast.makeText(GenerateOutfitActivity.this, "Error saving outfit", Toast.LENGTH_SHORT).show());
    }

    private void saveOutfitItems(String outfitId) {
        for(WardrobeItem w: selectedItemsOnCanvas){
            Log.d("GenerateOutfitActivity", "- " + w.getName());
        }
        for (WardrobeItem item : selectedItemsOnCanvas) {
            OutfitsItems outfitItems = new OutfitsItems(outfitId, item.getItemId(), item.getCategory());
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("outfitsItems").add(outfitItems);
        }
    }


    private void captureCanvasImage(String outfitId) {
        // Crează un obiect Bitmap pentru canvas
        FrameLayout canvas = findViewById(R.id.canvas_outfit);
        Bitmap bitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvasImage = new Canvas(bitmap);
        canvas.draw(canvasImage); // Desenează canvasul într-o imagine

        // Salvează imaginea în Firestore
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference outfitImageRef = storageReference.child("outfit_images/" + outfitId + ".png");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        outfitImageRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> {
                    outfitImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Salvează URL-ul imaginii în documentul Outfit
                        FirebaseFirestore.getInstance().collection("outfits").document(outfitId)
                                .update("image", uri.toString());
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(GenerateOutfitActivity.this, "Error capturing image", Toast.LENGTH_SHORT).show());
    }


    private void loadCategories() {
        categoryAdapter.notifyDataSetChanged();
        recyclerViewCategories.post(() -> {
            categoryAdapter.notifyDataSetChanged();
            recyclerViewCategories.scrollToPosition(0);
        });
        if (!categories.isEmpty()) {
            fetchWardrobeItemsByCategory(categories.get(0));
        }

    }

    private void fetchWardrobeItemsByCategory(String category) {
        db.collection("wardrobeItem")
                .whereEqualTo("Category", category)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        wardrobeItems.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            WardrobeItem item = document.toObject(WardrobeItem.class);
                            wardrobeItems.add(item);
                        }
                        smallWardrobeItemAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("Wardrobe", "Failed to fetch items: " + task.getException());
                        Toast.makeText(this, "Failed to fetch items", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addItemToCanvas(WardrobeItem item) {
        // Creăm un ImageView pentru articol
        ImageView imageView = new ImageView(this);
        Picasso.get().load(item.getImage()).into(imageView);

        imageView.setBackgroundColor(Color.TRANSPARENT);

        // Creștem dimensiunea imaginii pe canvas
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(600, 600); // Dimensiuni mai mari
        imageView.setLayoutParams(params);
        imageView.setX(100); // Poziție inițială X
        imageView.setY(100); // Poziție inițială Y
        canvasOutfit.addView(imageView);

        // Adăugăm articolul în lista `selectedItemsOnCanvas`
        selectedItemsOnCanvas.add(item);

        // Adăugăm un buton "X" pentru ștergere
        ImageView deleteButton = new ImageView(this);
        deleteButton.setImageResource(R.drawable.delete);
        FrameLayout.LayoutParams deleteParams = new FrameLayout.LayoutParams(50, 50);
        deleteButton.setLayoutParams(deleteParams);

        // Poziționăm butonul "X" în colțul imaginii
        deleteButton.setX(imageView.getX() + params.width - 90); // Poziție relativă
        deleteButton.setY(imageView.getY()); // Poziție relativă
        canvasOutfit.addView(deleteButton);

        // Adăugăm funcționalitate de mutare imaginii și sincronizare cu butonul "X"
        imageView.setOnTouchListener(new DragTouchListener(imageView, deleteButton));

        // Funcționalitate de ștergere
        deleteButton.setOnClickListener(v -> {
            // Eliminăm articolul din lista `selectedItemsOnCanvas`
            selectedItemsOnCanvas.remove(item);

            // Eliminăm imaginea și butonul de pe canvas
            canvasOutfit.removeView(imageView);
            canvasOutfit.removeView(deleteButton);
        });
    }




}