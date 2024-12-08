package com.example.dressfind.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dressfind.R;
import com.example.dressfind.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailView;
    private EditText firstNameView;
    private EditText lastNameView;
    private EditText passwordView;
    private EditText confirmPasswordView;
    private Button registerButton;
    private TextView loginLink;
    private FirebaseFirestore firestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        emailView = findViewById(R.id.email);
        firstNameView = findViewById(R.id.first_name);
        lastNameView = findViewById(R.id.last_name);
        passwordView = findViewById(R.id.password);
        confirmPasswordView = findViewById(R.id.confirm_password);
        registerButton = findViewById(R.id.register_button);
        loginLink = findViewById(R.id.login_link);

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailView.getText().toString().trim();
                String password = passwordView.getText().toString().trim();
                String confirmPassword = confirmPasswordView.getText().toString().trim();
                String firstName = firstNameView.getText().toString().trim();
                String lastName = lastNameView.getText().toString().trim();


                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Enter both email and password", Toast.LENGTH_SHORT).show();
                } else if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                }  else {
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = auth.getCurrentUser();

                                    Log.i("RegisterActivity", "Register task is successful");
                                    String userId = user.getUid();

                                    User userToSave = new User();
                                    userToSave.setUserId(userId);
                                    userToSave.setEmail(email);
                                    userToSave.setFirstName(firstName);
                                    userToSave.setLastName(lastName);

                                    Date date = new Date();
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        userToSave.setRegistrationDate(date);
                                    }

                                    Log.i("RegisterActivity", "user object: " + userToSave);

                                    firestore.collection("user").document(userId)
                                            .set(userToSave)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.i("RegisterActivity", "User registered: " + userToSave);
                                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("RegisterActivity",  "Failed to add user to db: " + e.getMessage());
                                                Toast.makeText(RegisterActivity.this, "Failed to save user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    Log.e("RegisterActivity", "Register task was not successful: " + task.getException().getMessage());
                                    Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

    }
}
