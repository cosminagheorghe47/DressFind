package com.example.dressfind.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dressfind.R;
import com.example.dressfind.models.PinterestProfile;
import com.example.dressfind.services.PinterestService;
import com.example.dressfind.services.SharedPreferencesService;
import com.squareup.picasso.Picasso;

public class PinterestCallbackActivity extends AppCompatActivity {

    private TextView tvUsername, tvBusinessName, tvFollowers, tvBoards, tvPins;
    private ImageView ivProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinterest_callback);
        SharedPreferencesService sharedPreferencesService = new SharedPreferencesService(this);
        initializeUI();

        Intent intent = getIntent();
        Uri uri = intent.getData();
        boolean fetchProfile = getIntent().getBooleanExtra("fetchProfile", false);

        if(fetchProfile)
        {
            fetchUserProfile(sharedPreferencesService.getAccessToken());
        }
        else if (uri != null && "com.example.dressfind".equals(uri.getScheme()) && "home".equals(uri.getHost())) {
            String authCode = uri.getQueryParameter("code");
            if (authCode != null) {
                handleAuthorizationCode(authCode);
            } else {
                Log.e("PinterestAPI", "Authorization code is null.");
            }
        }
        else {
            Log.e("PinterestAPI", "Invalid callback URI.");
        }
    }

    private void initializeUI() {
        tvUsername = findViewById(R.id.tvUsername);
        tvBusinessName = findViewById(R.id.tvBusinessName);
        tvFollowers = findViewById(R.id.tvFollowers);
        tvBoards = findViewById(R.id.tvBoards);
        tvPins = findViewById(R.id.tvPins);
        ivProfileImage = findViewById(R.id.ivProfileImage);

    }

    private void handleAuthorizationCode(String authCode) {
        PinterestService pinterestService = new PinterestService(this);
        pinterestService.exchangeAuthCodeForToken(authCode, new PinterestService.TokenCallback() {
            @Override
            public void onTokenReceived(String accessToken) {
                fetchUserProfile(accessToken);
            }

            @Override
            public void onError(String error) {
                Log.e("PinterestAPI", "Error: " + error);
            }
        });
    }

    private void fetchUserProfile(String accessToken) {
        PinterestService pinterestService = new PinterestService(this);
        pinterestService.fetchUserProfile(accessToken, new PinterestService.ProfileCallback() {
            @Override
            public void onProfileFetched(PinterestProfile profile) {
                updateUI(profile);
            }

            @Override
            public void onError(String error) {
                Log.e("PinterestAPI", "Profile Error: " + error);
            }
        });
    }

    private void updateUI(PinterestProfile profile) {
        runOnUiThread(() -> {
            tvUsername.setText("Username: " + profile.getUsername());
            tvBusinessName.setText("Business Name: " + profile.getBusinessName());
            tvFollowers.setText("Followers: " + profile.getFollowerCount());
            tvBoards.setText("Boards: "+ profile.getBoardCount());
            tvPins.setText("Pins: "+ profile.getPinCount());
            Picasso.get().load(profile.getProfileImage()).into(ivProfileImage);
        });
    }
}
