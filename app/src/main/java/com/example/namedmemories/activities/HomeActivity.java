package com.example.namedmemories.activities;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.namedmemories.R;
import androidx.appcompat.app.AppCompatActivity;


public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView userNameTextView = findViewById(R.id.userNameTextView);
        ImageView profileImageView = findViewById(R.id.profileImageView);

        // Load saved name and image URI from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userName = prefs.getString("userName", "User");
        String userImageUriString = prefs.getString("userImageUri", null);

        userNameTextView.setText(userName);

        if (userImageUriString != null) {
            Uri userImageUri = Uri.parse(userImageUriString);
            profileImageView.setImageURI(userImageUri);
        } else {
            // Set default image or color filter if no image is saved
            profileImageView.setImageResource(R.drawable.ic_person);
        }
    }
}
