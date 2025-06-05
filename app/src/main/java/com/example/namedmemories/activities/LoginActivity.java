package com.example.namedmemories.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.namedmemories.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText;
    private Button loginTabButton, signupTabButton, actionButton;
    private LinearLayout loginFooterLinks, signupFooterLinks;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        actionButton = findViewById(R.id.buttonLogin);
        loginTabButton = findViewById(R.id.buttonLoginTab);
        signupTabButton = findViewById(R.id.buttonSignupTab);

        // Footer links
        loginFooterLinks = findViewById(R.id.loginFooterLinks);
        signupFooterLinks = findViewById(R.id.signupFooterLinks);
        TextView signupLink = findViewById(R.id.textViewSignupLink);
        TextView forgotPasswordLink = findViewById(R.id.textViewForgotPassword);
        TextView loginLink = findViewById(R.id.textViewLoginLink);

        // Set up tab switching
        loginTabButton.setOnClickListener(v -> switchToLoginMode());
        signupTabButton.setOnClickListener(v -> switchToSignupMode());

        // Set up main action button
        actionButton.setOnClickListener(v -> {
            if (isLoginMode) {
                performLogin();
            } else {
                performSignup();
            }
        });

        // Set up footer links
        signupLink.setOnClickListener(v -> switchToSignupMode());
        loginLink.setOnClickListener(v -> switchToLoginMode());
        forgotPasswordLink.setOnClickListener(v -> handleForgotPassword());

        // Initialize in login mode
        switchToLoginMode();
    }

    @SuppressLint("SetTextI18n")
    private void switchToLoginMode() {
        isLoginMode = true;

        // Update tab appearance
        loginTabButton.setBackgroundResource(R.drawable.login_tab_selected);
        loginTabButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        signupTabButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        signupTabButton.setTextColor(ContextCompat.getColor(this, R.color.secondary_text_color));

        // Update button text
        actionButton.setText("Login");

        // Show/hide appropriate footer links
        loginFooterLinks.setVisibility(View.VISIBLE);
        signupFooterLinks.setVisibility(View.GONE);
    }

    @SuppressLint("SetTextI18n")
    private void switchToSignupMode() {
        isLoginMode = false;

        // Update tab appearance
        signupTabButton.setBackgroundResource(R.drawable.login_tab_selected);
        signupTabButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        loginTabButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        loginTabButton.setTextColor(ContextCompat.getColor(this, R.color.secondary_text_color));

        // Update button text
        actionButton.setText("Sign Up");

        // Show/hide appropriate footer links
        loginFooterLinks.setVisibility(View.GONE);
        signupFooterLinks.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    private void performLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        actionButton.setText("Logging in...");
        actionButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    // Reset button state
                    actionButton.setText("Login");
                    actionButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users").document(uid).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists() && Boolean.TRUE.equals(documentSnapshot.getBoolean("profileComplete"))) {
                                            // Go directly to HomeActivity, always using Firestore data
                                            Intent intent = new Intent(this, HomeActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        } else {
                                            // No profile in Firestore or incomplete, go to setup
                                            Intent intent = new Intent(this, SetupActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        }
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to fetch profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(this, SetupActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    });
                        } else {
                            Intent intent = new Intent(this, SetupActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        String errorMsg = "Login failed";
                        if (task.getException() != null) {
                            errorMsg += ": " + task.getException().getMessage();
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void performSignup() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        actionButton.setText("Creating account...");
        actionButton.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    // Reset button state
                    actionButton.setText("Sign Up");
                    actionButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                        // Go directly to setup activity for user details
                        startActivity(new Intent(this, SetupActivity.class));
                        finish();
                    } else {
                        String errorMsg = "Signup failed";
                        if (task.getException() != null) {
                            errorMsg += ": " + task.getException().getMessage();
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleForgotPassword() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email address first", Toast.LENGTH_SHORT).show();
            emailEditText.requestFocus();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset email sent to " + email, Toast.LENGTH_LONG).show();
                    } else {
                        String errorMsg = "Failed to send reset email";
                        if (task.getException() != null) {
                            errorMsg += ": " + task.getException().getMessage();
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }
}

