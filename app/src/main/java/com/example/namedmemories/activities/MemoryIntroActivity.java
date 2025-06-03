package com.example.namedmemories.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.namedmemories.R;

public class MemoryIntroActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_intro);

        Button continueButton = findViewById(R.id.continueButton);
        continueButton.setOnClickListener(view -> {
            Intent intent = new Intent(MemoryIntroActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
