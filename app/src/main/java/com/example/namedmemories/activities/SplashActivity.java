// activities/SplashActivity.java
package com.example.namedmemories.activities;
import com.example.namedmemories.R ;
import com.google.firebase.FirebaseApp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        FirebaseApp.initializeApp(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseAuth mAuthDelayed = FirebaseAuth.getInstance();
            FirebaseUser currentUserDelayed = mAuthDelayed.getCurrentUser();
            Intent intent;
            if (currentUserDelayed == null) {
                // Not logged in, go to LoginActivity
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            } else {
                // Logged in, check if setup is complete
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String userName = prefs.getString("userName", null);
                String userImagePath = prefs.getString("userImagePath", null);
                if (userName != null && userImagePath != null) {
                    intent = new Intent(SplashActivity.this, HomeActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, SetupActivity.class);
                }
            }
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}
