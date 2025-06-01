package com.example.namedmemories.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import com.example.namedmemories.R;

public class SetupActivity extends AppCompatActivity {

    private ImageView profileImage;
    private EditText nameInput;
    private Uri selectedImageUri;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        profileImage = findViewById(R.id.profileImage);
        nameInput = findViewById(R.id.nameInput);
        Button saveButton = findViewById(R.id.saveButton);

        // Setup image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            profileImage.setImageURI(selectedImageUri);
                        }
                    }
                }
        );

        // On image click → launch image picker
        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        // Save button click
        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();

            if (!name.isEmpty()) {
                // You can store the name and URI in SharedPreferences or pass to HomeActivity
                if (selectedImageUri != null) {
                    // Save name and image URI using SharedPreferences
                    getSharedPreferences("UserPrefs", MODE_PRIVATE)
                            .edit()
                            .putString("userName", name)
                            .putString("userImageUri", selectedImageUri.toString())
                            .apply();

                    // Navigate to HomeActivity
                    Intent intent = new Intent(SetupActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Optional: error for missing profile image
                    Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show();

                    // Or show a Toast or Snackbar
                }

            } else {
                nameInput.setError("Please enter your name");
            }
        });
    }
}
