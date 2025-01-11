package com.example.dressfind.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dressfind.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText emailView;
    private EditText passwordView;
    private Button loginButton;
    private TextView registerLink;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Log.i("LoginActivity", "User is already signed in: " + currentUser.getEmail());
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }


        emailView = findViewById(R.id.email);
        passwordView = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.register_link);

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailView.getText().toString().trim();
                String password = passwordView.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
                } else {
                    auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = auth.getCurrentUser();
                                    Log.i("LoginActivity", "User signed in: " + user.getEmail());
                                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                } else {
                                    Log.e("LoginActivity", "User login failed: " + task.getException().getMessage());
                                    Toast.makeText(LoginActivity.this, "Authentication failed" , Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

    }
}
