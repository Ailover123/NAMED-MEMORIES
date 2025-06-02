package com.example.namedmemories.activities;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.namedmemories.R;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView userNameTextView = findViewById(R.id.userNameTextView);
        TextView welcomeTextView = findViewById(R.id.welcomeTextView);
        ImageView profileImageView = findViewById(R.id.profileImageView);
        Button addMemoryButton = findViewById(R.id.addMemoryButton);
        Button viewTimelineButton = findViewById(R.id.viewTimelineButton);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userName = prefs.getString("userName", "User");
        String userImagePath = prefs.getString("userImagePath", null);

        userNameTextView.setText(userName);
        welcomeTextView.setText("Welcome, " + userName + "!");

        loadProfileImage(userImagePath, profileImageView);

        // Optional placeholder logic
        addMemoryButton.setOnClickListener(v ->
                Toast.makeText(this, "Add Memory clicked!", Toast.LENGTH_SHORT).show()
        );

        viewTimelineButton.setOnClickListener(v ->
                Toast.makeText(this, "View Timeline clicked!", Toast.LENGTH_SHORT).show()
        );
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
