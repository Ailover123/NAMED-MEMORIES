// activities/SplashActivity.java
package com.example.namedmemories.activities;
import com.example.namedmemories.R ;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Navigate to SetupActivity after delay
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String userName = prefs.getString("userName", null);
            String userImagePath = prefs.getString("userImagePath", null);

            Intent intent;
            if (userName != null && userImagePath != null) {
                // User has already set up profile
                intent = new Intent(SplashActivity.this, HomeActivity.class);
            } else {
                // First time setup
                intent = new Intent(SplashActivity.this, SetupActivity.class);
            }
            startActivity(intent);

            finish(); // finish splash so it's not in back stack
        }, SPLASH_DELAY);
    }
}
