package com.example.dressfind.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.dressfind.R;
import com.example.dressfind.services.PinterestService;
import com.example.dressfind.services.SharedPreferencesService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView ;
    CardView scannedProductsCard;
    CardView MyWardrobeCard;
    CardView MyOutfitsCard;
    private static final int PINTEREST_AUTH_REQUEST_CODE = 123;
    CardView ScanProductCard;
    CardView ConnectPinterest;
    private ActivityResultLauncher<Intent> pinterestAuthLauncher;

    TextView seePinterestAccTV ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        SharedPreferencesService sharedPreferencesService = new SharedPreferencesService(this);
        bottomNavigationView = findViewById(R.id.includeNavBar);
        PinterestService pinterestService = new PinterestService(this);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        seePinterestAccTV = findViewById(R.id.connectPinterestTV);

        if(!sharedPreferencesService.isAccessTokenExpired() && sharedPreferencesService.getAccessToken()!=null)
            seePinterestAccTV.setText("See Pinterest Profile");

        pinterestAuthLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                Uri uri = data.getData();
                                Log.e("PinterestAPI", "Returned URI: " + uri.toString());

                                if (uri != null && "com.example.dressfind".equals(uri.getScheme()) && "home".equals(uri.getHost())) {
                                    String authCode = uri.getQueryParameter("code");
                                    Log.e("PinterestAPI", "Authorization code: " + authCode);

                                    if (authCode != null) {
                                        // Redirect to PinterestCallbackActivity with the authCode
                                        Intent callbackIntent = new Intent(HomeActivity.this, PinterestCallbackActivity.class);
                                        callbackIntent.putExtra("authCode", authCode);
                                        startActivity(callbackIntent);
                                    } else {
                                        Log.e("PinterestAPI", "Authorization code not found.");
                                    }
                                }
                            }
                        }
                    }
                });
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                return true;
            } else if (item.getItemId() == R.id.nav_outfits) {
                Intent scanIntent = new Intent(HomeActivity.this, MyOutfitsActivity.class);
                startActivity(scanIntent);
                overridePendingTransition(0, 0);
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
        ConnectPinterest = findViewById(R.id.connectToPinterestCard);
        ConnectPinterest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sharedPreferencesService.getAccessToken()!=null && sharedPreferencesService.isAccessTokenExpired()) {
                    pinterestService.refreshAccessToken(sharedPreferencesService.getRefreshToken(), new PinterestService.TokenCallback() {
                        @Override
                        public void onTokenReceived(String newAccessToken) {
                            sharedPreferencesService.saveAccessToken(newAccessToken);
                            Intent callbackIntent = new Intent(HomeActivity.this, PinterestCallbackActivity.class);
                            callbackIntent.putExtra("fetchProfile", true);
                            startActivity(callbackIntent);
                        }

                        @Override
                        public void onError(String message) {
                            Log.e("PinterestAPI", "Error refreshing access token in HomeActivity");

                        }
                    });
                }
                else if(sharedPreferencesService.getAccessToken()!=null && !sharedPreferencesService.isAccessTokenExpired())
                {
                    Intent callbackIntent = new Intent(HomeActivity.this, PinterestCallbackActivity.class);
                    callbackIntent.putExtra("fetchProfile", true);
                    startActivity(callbackIntent);
                }
                else {
                    //connect to pinterest logic


//                Log.e("PinterestAPI", "tokens and expiries: " + sharedPreferencesService.getAccessToken() + "    " + (sharedPreferencesService.getRefreshToken()!=null) + "    "+ (sharedPreferencesService.isRefreshTokenExpired())+ " "+ sharedPreferencesService.isAccessTokenExpired());
                    Log.e("PinterestAPI", "Starting to connect to Pinterest");
                    String clientId = "1509034"; //app id
                    String redirectUri = "com.example.dressfind://home";
                    String scopes = "boards:read,pins:read,user_accounts:read";
                    String authUrl = "https://www.pinterest.com/oauth/?" +
                            "client_id=" + clientId + "&" +
                            "redirect_uri=" + redirectUri + "&" +
                            "response_type=code&" +
                            "scope=" + scopes;
                    Log.e("PinterestAPI", "authUrl: " + authUrl);

                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
                    pinterestAuthLauncher.launch(browserIntent);

                }

            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }






}