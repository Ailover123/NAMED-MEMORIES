// activities/SplashActivity.java
package com.example.namedmemories.activities;
import com.example.namedmemories.R ;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.FirebaseApp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

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

        // Animate logo
        ShapeableImageView splashLogo = findViewById(R.id.splashLogo);
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
        splashLogo.startAnimation(bounce);

        // Optional: Apply gradient color to splashText
        TextView splashText = findViewById(R.id.splashText);
        Shader textShader = new LinearGradient(
                0, 0, splashText.getWidth(), 0,  // Horizontal
                new int[]{
                        Color.parseColor("#6A11CB"),  // Purplish Blue
                        Color.parseColor("#2575FC")   // Lighter Violet
                },
                null, Shader.TileMode.CLAMP);
        splashText.getPaint().setShader(textShader);
        splashText.invalidate();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseAuth mAuthDelayed = FirebaseAuth.getInstance();
            FirebaseUser currentUserDelayed = mAuthDelayed.getCurrentUser();
            Intent intent;
            if (currentUserDelayed == null) {
                // Not logged in, go to MemoryIntroActivity first
                intent = new Intent(SplashActivity.this, MemoryIntroActivity.class);
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
