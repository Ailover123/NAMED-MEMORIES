package com.example.namedmemories.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.namedmemories.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

public class SetupActivity extends AppCompatActivity {

    private ImageView profileImage;
    private EditText nameInput;
    private EditText birthdayInput;
    private Uri selectedImageUri;
    private String selectedBirthday = "";

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        profileImage = findViewById(R.id.profileImage);
        nameInput = findViewById(R.id.nameInput);
        birthdayInput = findViewById(R.id.birthdayInput);
        Button saveButton = findViewById(R.id.saveButton);

        // Setup image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            profileImage.setImageURI(selectedImageUri);
                            saveProfileImageToInternalStorage(selectedImageUri);
                        }
                    }
                }
        );

        // On image click → check permission, then launch image picker
        profileImage.setOnClickListener(v -> {
            if (hasStoragePermission()) {
                launchImagePicker();
            } else {
                requestStoragePermission();
            }
        });

        // Birthday input click → show date picker
        birthdayInput.setOnClickListener(v -> showDatePicker());

        // Save button click
        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String birthday = birthdayInput.getText().toString().trim();

            if (name.isEmpty()) {
                nameInput.setError("Please enter your name");
                return;
            }

            if (birthday.isEmpty()) {
                birthdayInput.setError("Please select your birthday");
                return;
            }

            File file = new File(getFilesDir(), "profile_image.jpg");
            if (!file.exists()) {
                Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save user data
            getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    .edit()
                    .putString("userName", name)
                    .putString("userBirthday", selectedBirthday)
                    .apply();

            // Navigate to HomeActivity
            Intent intent = new Intent(SetupActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        @SuppressLint("DefaultLocale") DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format the date for display
                    @SuppressLint("DefaultLocale") String formattedDate = String.format("%02d/%02d/%d",
                            selectedDay, selectedMonth + 1, selectedYear);
                    birthdayInput.setText(formattedDate);

                    // Store the date in a standard format for storage
                    selectedBirthday = String.format("%d-%02d-%02d",
                            selectedYear, selectedMonth + 1, selectedDay);
                },
                year, month, day
        );

        // Set maximum date to today (can't be born in the future)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        // Set minimum date to 100 years ago (reasonable limit)
        calendar.add(Calendar.YEAR, -100);
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        datePickerDialog.show();
    }

    private void saveProfileImageToInternalStorage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                File file = new File(getFilesDir(), "profile_image.jpg");
                OutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.close();
                inputStream.close();
                // Save the file path in SharedPreferences
                getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        .edit()
                        .putString("userImagePath", file.getAbsolutePath())
                        .apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
        }
    }

    private void launchImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private boolean hasStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, 2);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2 && grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            launchImagePicker();
        } else if (requestCode == 2) {
            Toast.makeText(this, "Permission denied. Cannot select profile image.", Toast.LENGTH_SHORT).show();
        }
    }
}