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

import com.example.namedmemories.R;

public class SetupActivity extends AppCompatActivity {

    private ImageView profileImage;
    private EditText nameInput;
    private Button saveButton;
    private Uri selectedImageUri;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        profileImage = findViewById(R.id.profileImage);
        nameInput = findViewById(R.id.nameInput);
        saveButton = findViewById(R.id.saveButton);

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
                Intent intent = new Intent(SetupActivity.this, HomeActivity.class);
                intent.putExtra("userName", name);
                if (selectedImageUri != null) {
                    intent.putExtra("userImageUri", selectedImageUri.toString());
                }
                startActivity(intent);
                finish();
            } else {
                nameInput.setError("Please enter your name");
            }
        });
    }
}
