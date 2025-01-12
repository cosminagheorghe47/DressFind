package com.example.dressfind.activities;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dressfind.R;
import com.example.dressfind.models.Outfit;
import com.example.dressfind.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ExploreActivity extends AppCompatActivity {

    private DatabaseReference outfitRef;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    private RecyclerView outfitsRecyclerView;
    private OutfitsAdapter outfitsAdapter;

    private List<Outfit> cachedOutfits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        outfitsRecyclerView = findViewById(R.id.outfitsRecyclerView);
        outfitsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        outfitRef = FirebaseDatabase.getInstance().getReference("outfits");
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        cachedOutfits = new ArrayList<>();

        if (!cachedOutfits.isEmpty()) {
            populateOutfitsList(cachedOutfits);
        } else {
            fetchPublicOutfits();
        }
    }

    private void fetchPublicOutfits() {
        db.collection("outfits")
                .whereEqualTo("isPublic", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Outfit> outfitsList = new ArrayList<>();

                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Outfit outfit = snapshot.toObject(Outfit.class);
                        if (outfit != null) {
                            outfitsList.add(outfit);
                        }
                    }

                    cachedOutfits.clear();
                    cachedOutfits.addAll(outfitsList);

                    populateOutfitsList(outfitsList);
                })
                .addOnFailureListener(e -> {
                    Log.w("ExploreActivity", "fetchPublicOutfits:onFailure", e);
                });
    }

    private void populateOutfitsList(List<Outfit> outfitsList) {
        outfitsAdapter = new OutfitsAdapter(outfitsList);
        outfitsRecyclerView.setAdapter(outfitsAdapter);
    }

    private class OutfitsAdapter extends RecyclerView.Adapter<OutfitsAdapter.OutfitViewHolder> {

        private List<Outfit> outfitsList;

        public OutfitsAdapter(List<Outfit> outfitsList) {
            this.outfitsList = outfitsList;
        }

        @NonNull
        @Override
        public OutfitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.public_outfit, parent, false);
            return new OutfitViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OutfitViewHolder holder, int position) {
            Outfit outfit = outfitsList.get(position);

            holder.outfitName.setText(outfit.getName());

            // Formatting the date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(outfit.getCreationDate());
            holder.creationDate.setText(formattedDate);

            fetchUserData(outfit.getUserId(), holder);

            Picasso.get()
                    .load(outfit.getImage())
                    .into(holder.outfitImage);
        }

        private void fetchUserData(String userId, OutfitViewHolder holder) {
            db.collection("user").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                holder.username.setText(user.getFirstName() + " " + user.getLastName());
                                loadProfileImage(user.getUserId(), holder.profileImage);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w("ExploreActivity", "fetchUserData:onFailure", e);
                    });
        }

        private void loadProfileImage(String userId, ImageView imageView) {
            StorageReference userImageRef = storageRef.child("profile_images/" + userId);
            userImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Picasso.get().load(uri).into(imageView);
            }).addOnFailureListener(e -> {
                Log.w("ExploreActivity", "loadProfileImage:onFailure", e);
            });
        }

        @Override
        public int getItemCount() {
            return outfitsList.size();
        }

        public class OutfitViewHolder extends RecyclerView.ViewHolder {
            ImageView outfitImage;
            ImageView profileImage;
            TextView outfitName;
            TextView username;
            TextView creationDate;

            public OutfitViewHolder(@NonNull View itemView) {
                super(itemView);
                outfitImage = itemView.findViewById(R.id.outfitImage);
                profileImage = itemView.findViewById(R.id.profileImage);
                outfitName = itemView.findViewById(R.id.outfitName);
                username = itemView.findViewById(R.id.username);
                creationDate = itemView.findViewById(R.id.creationDate);
            }
        }
    }
}
