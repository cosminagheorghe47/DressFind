package com.example.dressfind.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dressfind.R;
import com.example.dressfind.models.User;
import com.example.dressfind.services.PinterestService;
import com.example.dressfind.services.SharedPreferencesService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView ;

    private ActivityResultLauncher<Intent> pinterestAuthLauncher;
    private static final String TAG = "PinterestAPI";


    private static final int PINTEREST_AUTH_REQUEST_CODE = 123;
    private ImageView imageViewProfile;
    private Button buttonSelectImage;
    private TextView textViewFirstName;
    private TextView textViewLastName;
    private TextView textViewEmail;
    private TextView textViewRegistrationDate;
    TextView seePinterestAccTV ;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Uri imageUri;


    CardView ConnectPinterest;
    CardView SearchedProducts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imageViewProfile = findViewById(R.id.image_view_profile);
        buttonSelectImage = findViewById(R.id.button_select_image);
        textViewFirstName = findViewById(R.id.text_view_first_name);
        textViewLastName = findViewById(R.id.text_view_last_name);
        textViewEmail = findViewById(R.id.text_view_email);
        textViewRegistrationDate = findViewById(R.id.text_view_registration_date);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        checkForExistingProfileImage();
        fetchUserProfileData();


        SharedPreferencesService sharedPreferencesService = new SharedPreferencesService(this);
        PinterestService pinterestService = new PinterestService(this);

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
                                        Intent callbackIntent = new Intent(ProfileActivity.this, PinterestCallbackActivity.class);
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


        buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });


        bottomNavigationView = findViewById(R.id.includeNavBar);

        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_profile) {
                return true;
            } else if (item.getItemId() == R.id.nav_scan) {
                Intent scanIntent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(scanIntent);
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_home) {
                Intent scanIntent = new Intent(ProfileActivity.this, ExploreActivity.class);
                startActivity(scanIntent);
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_outfits) {
                Intent scanIntent = new Intent(ProfileActivity.this, MyOutfitsActivity.class);
                startActivity(scanIntent);
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_wardrobe) {
                Intent profileIntent = new Intent(ProfileActivity.this, MyWardrobeActivity.class);
                startActivity(profileIntent);
                return true;
            }else return true;
        });

        SearchedProducts = findViewById(R.id.scannedProductsCard);
        SearchedProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent searchIntent = new Intent(ProfileActivity.this, HistoryActivity.class);
                startActivity(searchIntent);
            }
        });


        ConnectPinterest = findViewById(R.id.connectToPinterestCard);
        ConnectPinterest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sharedPreferencesService.getAccessToken()!=null && sharedPreferencesService.isAccessTokenExpired()) {
                    Log.e(TAG,"Pinterest Token not null and token is expired.");
                    pinterestService.refreshAccessToken(sharedPreferencesService.getRefreshToken(), new PinterestService.TokenCallback() {
                        @Override
                        public void onTokenReceived(String newAccessToken) {
                            sharedPreferencesService.saveAccessToken(newAccessToken);
                            Log.e(TAG,"Pinterest Token refreshed.");
                            Intent callbackIntent = new Intent(ProfileActivity.this, PinterestCallbackActivity.class);
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
                    Log.e(TAG,"Pinterest Token not null and token NOT expired.");
                    Intent callbackIntent = new Intent(ProfileActivity.this, PinterestCallbackActivity.class);
                    callbackIntent.putExtra("fetchProfile", true);
                    startActivity(callbackIntent);
                }
                else {
                    //connect to pinterest logic

                    Log.e(TAG,"First connection to spotify cuz token is null");
//                Log.e("PinterestAPI", "tokens and expiries: " + sharedPreferencesService.getAccessToken() + "    " + (sharedPreferencesService.getRefreshToken()!=null) + "    "+ (sharedPreferencesService.isRefreshTokenExpired())+ " "+ sharedPreferencesService.isAccessTokenExpired());
                    Log.e("PinterestAPI", "Starting to connect to Pinterest");
                    String clientId = "1509034"; //app id
                    String redirectUri = "com.example.dressfind://home";
                    String scopes = "boards:read,pins:read,user_accounts:read,boards:write,pins:write";
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

    private void checkForExistingProfileImage() {
        String userId = auth.getCurrentUser().getUid();
        StorageReference userImageRef = storageReference.child("profile_images/" + userId);

        userImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(imageViewProfile);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ProfileActivity.this, "No existing profile image found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserProfileData() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("user").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                textViewFirstName.setText("First Name: " + user.getFirstName());
                                textViewLastName.setText("Last Name: " + user.getLastName());
                                textViewEmail.setText("Email: " + user.getEmail());

                                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                                String formattedDate = dateFormat.format(user.getRegistrationDate());
                                textViewRegistrationDate.setText("Registration Date: " + formattedDate);
                            } else {
                                Toast.makeText(ProfileActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ProfileActivity.this, "No profile found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(ProfileActivity.this, "Failed to fetch profile data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(imageViewProfile);
            uploadImage();
        }
    }

    private void uploadImage() {
        if (imageUri != null) {
            StorageReference fileReference = storageReference.child("profile_images/" + auth.getCurrentUser().getUid());

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    // updateUserProfileWithImageUrl(imageUrl);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(ProfileActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
        }
    }
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
    }
}
