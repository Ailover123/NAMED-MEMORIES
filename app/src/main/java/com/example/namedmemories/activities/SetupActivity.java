package com.example.namedmemories.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.namedmemories.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SetupActivity extends AppCompatActivity {

    private static final String TAG = "SetupActivity";
    private static final String USER_PREFS = "UserPrefs";
    private static final String PROFILE_IMAGE_FILENAME = "profile_image.jpg";
    private static final int MIN_NAME_LENGTH = 2;
    private static final int PERMISSION_REQUEST_CODE = 100;

    // UI Components
    private ImageView profileImage;
    private EditText nameInput;
    private EditText birthdayInput;
    private Button saveButton;
    private ProgressBar progressBar;
    private TextView progressText;

    private String selectedBirthday = "";
    private File profileImageFile;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        initializeComponents();
        setupPermissionHandling();
        setupImagePicker();
        setupClickListeners();
        setupBackPressedCallback();
    }

    private void initializeComponents() {
        initializeViews();
        initializeFirebase();
        initializeFileSystem();
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profileImage);
        nameInput = findViewById(R.id.nameInput);
        birthdayInput = findViewById(R.id.birthdayInput);
        saveButton = findViewById(R.id.saveButton);
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "User not authenticated, redirecting to login");
            redirectToLogin();
        }
    }

    private void initializeFileSystem() {
        profileImageFile = new File(getFilesDir(), PROFILE_IMAGE_FILENAME);
    }

    private void setupPermissionHandling() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        launchImagePicker();
                    } else {
                        showToast("Permission required to select profile image");
                    }
                }
        );
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            handleImageSelection(imageUri);
                        }
                    }
                }
        );
    }

    private void setupClickListeners() {
        profileImage.setOnClickListener(v -> handleProfileImageClick());
        birthdayInput.setOnClickListener(v -> showDatePicker());
        saveButton.setOnClickListener(v -> validateAndSaveProfile());
    }

    private void setupBackPressedCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isLoading()) {
                    showToast("Please wait while saving your profile...");
                } else {
                    finish();
                }
            }
        });
    }

    private void handleProfileImageClick() {
        if (hasStoragePermission()) {
            launchImagePicker();
        } else {
            requestStoragePermission();
        }
    }

    private void handleImageSelection(Uri imageUri) {
        // Data
        profileImage.setImageURI(imageUri);

        // Save image to internal storage in background
        new Thread(() -> {
            try {
                saveImageToInternalStorage(imageUri);
                runOnUiThread(() -> showToast("Image selected successfully"));
            } catch (IOException e) {
                Log.e(TAG, "Failed to save image", e);
                runOnUiThread(() -> showToast("Error saving image. Please try again."));
            }
        }).start();
    }

    private void validateAndSaveProfile() {
        if (!validateInputs()) {
            return;
        }

        String name = nameInput.getText().toString().trim();
        String birthday = birthdayInput.getText().toString().trim();

        saveUserProfile(name, birthday, profileImageFile.getAbsolutePath());
    }

    private boolean validateInputs() {
        String name = nameInput.getText().toString().trim();
        String birthday = birthdayInput.getText().toString().trim();

        // Validate name
        if (TextUtils.isEmpty(name)) {
            setFieldError(nameInput, "Please enter your name");
            return false;
        }

        if (name.length() < MIN_NAME_LENGTH) {
            setFieldError(nameInput, "Name must be at least " + MIN_NAME_LENGTH + " characters");
            return false;
        }

        // Validate birthday
        if (TextUtils.isEmpty(birthday)) {
            setFieldError(birthdayInput, "Please select your birthday");
            return false;
        }

        // Validate profile image
        if (!profileImageFile.exists()) {
            showToast("Please select a profile image");
            return false;
        }

        return true;
    }

    private void setFieldError(EditText field, String error) {
        field.setError(error);
        field.requestFocus();
    }

    private void saveUserProfile(String name, String birthday, String imagePath) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            showToast("Authentication error. Please login again.");
            redirectToLogin();
            return;
        }

        setLoading(true);

        Map<String, Object> userProfile = createUserProfileMap(name, imagePath, user.getUid());

        db.collection("users")
                .document(user.getUid())
                .set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User profile saved successfully");
                    saveUserDataLocally(name, birthday, imagePath);
                    navigateToHome();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save user profile", e);
                    setLoading(false);
                    showToast("Error saving profile: " + getErrorMessage(e));
                });
    }

    private Map<String, Object> createUserProfileMap(String name, String imagePath, String userId) {
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("name", name);
        userProfile.put("birthday", selectedBirthday);
        userProfile.put("profileImagePath", imagePath);
        userProfile.put("profileComplete", true);
        userProfile.put("createdAt", FieldValue.serverTimestamp());
        userProfile.put("userId", userId);
        return userProfile;
    }

    private void saveUserDataLocally(String name, String birthday, String imagePath) {
        SharedPreferences prefs = getSharedPreferences(USER_PREFS, MODE_PRIVATE);
        prefs.edit()
                .putString("userName", name)
                .putString("userBirthday", birthday)
                .putString("userImagePath", imagePath)
                .putBoolean("profileComplete", true)
                .apply();
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this::onDateSelected,
                year, month, day
        );

        // Set date constraints
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        calendar.add(Calendar.YEAR, -100);
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        datePickerDialog.show();
    }

    @SuppressLint("DefaultLocale")
    private void onDateSelected(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
        // Format for display (DD/MM/YYYY)
        String displayDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);
        birthdayInput.setText(displayDate);

        // Store in ISO format (YYYY-MM-DD)
        selectedBirthday = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth);
    }

    private void saveImageToInternalStorage(Uri imageUri) throws IOException {
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri);
             FileOutputStream outputStream = new FileOutputStream(profileImageFile)) {

            if (inputStream == null) {
                throw new IOException("Failed to open input stream");
            }

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    private void launchImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private boolean hasStoragePermission() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        return ContextCompat.checkSelfPermission(this, permission) ==
                getPackageManager().PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        permissionLauncher.launch(permission);
    }

    private void setLoading(boolean loading) {
        int visibility = loading ? View.VISIBLE : View.GONE;
        progressBar.setVisibility(visibility);
        progressText.setVisibility(visibility);

        float alpha = loading ? 0.5f : 1.0f;
        boolean enabled = !loading;

        saveButton.setEnabled(enabled);
        saveButton.setAlpha(alpha);
        nameInput.setEnabled(enabled);
        birthdayInput.setEnabled(enabled);
        profileImage.setClickable(enabled);
    }

    private boolean isLoading() {
        return progressBar.getVisibility() == View.VISIBLE;
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String getErrorMessage(Exception e) {
        return e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == getPackageManager().PERMISSION_GRANTED) {
                launchImagePicker();
            } else {
                showToast("Permission denied. Cannot select profile image.");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup is handled automatically by the activity result launchers
        Log.d(TAG, "SetupActivity destroyed");
    }
}