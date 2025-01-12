package com.example.dressfind.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.dressfind.R;
import com.example.dressfind.adapters.HistoryAdapter;
import com.example.dressfind.models.ScannedImage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private FirebaseAuth auth;
    private List<ScannedImage> scannedImagesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.recycler_view_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        historyAdapter = new HistoryAdapter(this, scannedImagesList);
        recyclerView.setAdapter(historyAdapter);

        fetchScannedImages();
    }

    private void fetchScannedImages() {
        db.collection("scannedImages")
                .whereEqualTo("userId", getUserIdFromAuth())
                .orderBy("scanDate", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot value, FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("Firestore", "Error fetching data", error);
                            return;
                        }

                        scannedImagesList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            ScannedImage scannedImage = doc.toObject(ScannedImage.class);
                            scannedImagesList.add(scannedImage);
                        }
                        historyAdapter.notifyDataSetChanged();
                    }
                });

    }


    private String getUserIdFromAuth() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
