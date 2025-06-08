package com.example.namedmemories.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.namedmemories.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.SetOptions;

public class ProfileRepository {
    private static final String TAG = "ProfileRepository";
    private static final String USERS_COLLECTION = "users";

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private static ProfileRepository instance;

    private ProfileRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Persistence is now enabled by default in newer Firebase versions
        // No need to configure settings manually
    }

    public static synchronized ProfileRepository getInstance() {
        if (instance == null) {
            instance = new ProfileRepository();
        }
        return instance;
    }

    // Interface for callbacks
    public interface ProfileCallback {
        void onSuccess(User user);
        void onFailure(String error);
    }

    public interface SaveCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // Save user profile with comprehensive error handling
    public void saveUserProfile(User user, SaveCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();
        user.setUserId(userId);

        if (user.getEmail() == null && currentUser.getEmail() != null) {
            user.setEmail(currentUser.getEmail());
        }

        DocumentReference userRef = db.collection(USERS_COLLECTION).document(userId);

        Log.d(TAG, "Attempting to save profile for user: " + userId);

        // Use set with merge option to update existing document or create new one
        userRef.set(user.toMap(), SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Profile saved successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving profile", e);
                    String errorMessage = getErrorMessage(e);
                    callback.onFailure(errorMessage);
                });
    }

    // Get user profile
    public void getUserProfile(String userId, ProfileCallback callback) {
        if (userId == null) {
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser == null) {
                callback.onFailure("User not authenticated");
                return;
            }
            userId = currentUser.getUid();
        }

        DocumentReference userRef = db.collection(USERS_COLLECTION).document(userId);

        Log.d(TAG, "Fetching profile for user: " + userId);

        userRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            try {
                                User user = document.toObject(User.class);
                                if (user != null) {
                                    Log.d(TAG, "Profile fetched successfully");
                                    callback.onSuccess(user);
                                } else {
                                    callback.onFailure("Failed to parse user data");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing user data", e);
                                callback.onFailure("Error parsing user data: " + e.getMessage());
                            }
                        } else {
                            Log.d(TAG, "No profile found");
                            callback.onFailure("No profile found");
                        }
                    } else {
                        Log.e(TAG, "Error fetching profile", task.getException());
                        String errorMessage = getErrorMessage(task.getException());
                        callback.onFailure(errorMessage);
                    }
                });
    }

    // Update specific fields
    public void updateUserField(String field, Object value, SaveCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();
        DocumentReference userRef = db.collection(USERS_COLLECTION).document(userId);

        userRef.update(field, value)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Field updated successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating field", e);
                    callback.onFailure(getErrorMessage(e));
                });
    }

    // Retry mechanism for failed operations
    public void saveUserProfileWithRetry(User user, SaveCallback callback) {
        saveUserProfileWithRetry(user, callback, 3);
    }

    private void saveUserProfileWithRetry(User user, SaveCallback callback, int attemptsLeft) {
        if (attemptsLeft <= 0) {
            callback.onFailure("Max retry attempts exceeded");
            return;
        }

        saveUserProfile(user, new SaveCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }

            @Override
            public void onFailure(String error) {
                Log.d(TAG, "Save attempt failed, retries left: " + (attemptsLeft - 1));
                if (attemptsLeft > 1) {
                    // Wait 2 seconds before retrying - Fixed Handler deprecation
                    new Handler(Looper.getMainLooper()).postDelayed(() ->
                            saveUserProfileWithRetry(user, callback, attemptsLeft - 1), 2000);
                } else {
                    callback.onFailure(error);
                }
            }
        });
    }

    // Helper method to get user-friendly error messages
    private String getErrorMessage(Exception e) {
        if (e == null) return "Unknown error occurred";

        String message = e.getMessage();
        if (message == null) return "Unknown error occurred";

        if (message.contains("PERMISSION_DENIED")) {
            return "Permission denied. Please check your Firestore security rules.";
        } else if (message.contains("UNAVAILABLE")) {
            return "Network unavailable. Please check your internet connection.";
        } else if (message.contains("DEADLINE_EXCEEDED")) {
            return "Request timeout. Please try again.";
        } else if (message.contains("UNAUTHENTICATED")) {
            return "User not authenticated. Please sign in again.";
        } else {
            return "Error: " + message;
        }
    }
}