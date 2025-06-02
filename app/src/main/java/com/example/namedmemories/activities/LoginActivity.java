package com.example.namedmemories.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.namedmemories.R;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        Button loginButton = findViewById(R.id.buttonLogin);
        TextView signupLink = findViewById(R.id.textViewSignupLink);

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password are required", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Check if setup is complete
                            android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            String userName = prefs.getString("userName", null);
                            String userImagePath = prefs.getString("userImagePath", null);
                            if (userName != null && userImagePath != null) {
                                startActivity(new Intent(this, HomeActivity.class));
                            } else {
                                startActivity(new Intent(this, SetupActivity.class));
                            }
                            finish();
                        } else {
                            String errorMsg = "Login failed";
                            if (task.getException() != null) {
                                errorMsg += ": " + task.getException().getMessage();
                            }
                            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        signupLink.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
    }
}
