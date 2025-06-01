package com.example.namedmemories.activities;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.namedmemories.R;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView userNameTextView = findViewById(R.id.userNameTextView);
        ImageView profileImageView = findViewById(R.id.profileImageView);

        // Load saved name and image path from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userName = prefs.getString("userName", "User");
        String userImagePath = prefs.getString("userImagePath", null);

        userNameTextView.setText(userName);

        loadProfileImage(userImagePath, profileImageView);
    }

    private void loadProfileImage(String userImagePath, ImageView profileImageView) {
        if (userImagePath != null) {
            File file = new File(userImagePath);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(userImagePath);
                profileImageView.setImageBitmap(bitmap);
            } else {
                profileImageView.setImageResource(R.drawable.ic_person);
            }
        } else {
            profileImageView.setImageResource(R.drawable.ic_person);
        }
    }
}
