package com.example.traveljurnalapp;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            try {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser != null) {
                    String email = currentUser.getEmail();
                    Log.d("FirebaseAuth", "Logged in user: " + (email != null ? email : "No email found"));
                    startActivity(new Intent(SplashActivity.this, MapActivity.class));
                } else {
                    Log.d("FirebaseAuth", "No user logged in.");
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
            } catch (Exception e) {
                Log.e("SplashActivity", "Error during splash: " + e.getMessage(), e);
            }

            finish();
        }, SPLASH_DELAY);
    }
}