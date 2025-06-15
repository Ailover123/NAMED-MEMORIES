package com.example.namedmemories.activities;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
import android.view.View;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import android.content.Intent;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import com.example.namedmemories.R;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private TextView welcomeTextView;
    private TextView memoryCountTextView;
    private TextView streakCountTextView;
    private TextView peopleCountTextView;
    private ImageView profileImageView;
    private ImageButton menuButton;
    private Button logoutButton;

    // Feature Cards
    private CardView addMemoryCard;
    private CardView viewMemoriesCard;
    private CardView calendarCard;
    private CardView messagesCard;
    private CardView managePeopleCard;

    // Event Views
    private TextView viewAllEventsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeViews();
        loadUserData();
        setUpClickListeners();
        updateWelcomeMessage();
        loadStatsData();
    }

    private void initializeViews() {
        // Header views
        userNameTextView = findViewById(R.id.userNameTextView);
        welcomeTextView = findViewById(R.id.welcomeTextView);
        profileImageView = findViewById(R.id.profileImageView);
        menuButton = findViewById(R.id.menuButton);

        // Stats views
        memoryCountTextView = findViewById(R.id.memoryCountTextView);
        streakCountTextView = findViewById(R.id.streakCountTextView);
        peopleCountTextView = findViewById(R.id.peopleCountTextView);

        // Event views
        viewAllEventsTextView = findViewById(R.id.viewAllEventsTextView);

        // Feature cards
        addMemoryCard = findViewById(R.id.addMemoryCard);
        viewMemoriesCard = findViewById(R.id.viewMemoriesCard);
        calendarCard = findViewById(R.id.calendarCard);
        messagesCard = findViewById(R.id.messagesCard);
        managePeopleCard = findViewById(R.id.managePeopleCard);

        // Logout button
        logoutButton = findViewById(R.id.logoutButton);
    }

    private void loadUserData() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && Boolean.TRUE.equals(documentSnapshot.getBoolean("profileComplete"))) {
                            String name = documentSnapshot.getString("name");
                            String imagePath = documentSnapshot.getString("profileImagePath");
                            userNameTextView.setText(name);
                            loadProfileImage(imagePath, profileImageView);
                        } else {
                            // Redirect to setup if not found or profile incomplete
                            Intent intent = new Intent(this, SetupActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
    }

    private void updateWelcomeMessage() {
        // Get current time and create personalized welcome message
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        String timeGreeting;

        if (hour < 12) {
            timeGreeting = "Good morning!";
        } else if (hour < 17) {
            timeGreeting = "Good afternoon!";
        } else {
            timeGreeting = "Good evening!";
        }

        welcomeTextView.setText(timeGreeting);
    }

    private void loadStatsData() {
        SharedPreferences prefs = getSharedPreferences("AppStats", MODE_PRIVATE);

        // Load saved stats or use defaults
        int memoryCount = prefs.getInt("memoryCount", 0);
        int streakCount = prefs.getInt("streakCount", 0);
        int peopleCount = prefs.getInt("peopleCount", 1);

        memoryCountTextView.setText(String.valueOf(memoryCount));
        streakCountTextView.setText(String.valueOf(streakCount));
        peopleCountTextView.setText(String.valueOf(peopleCount));
    }

    private void setUpClickListeners() {
        // Menu button
        menuButton.setOnClickListener(v -> {
            animateClick(menuButton);
            showMenuOptions();
        });

        // Feature cards with animations
        addMemoryCard.setOnClickListener(v -> {
            animateClick(addMemoryCard);
            navigateToAddMemory();
        });

        viewMemoriesCard.setOnClickListener(v -> {
            animateClick(viewMemoriesCard);
            navigateToViewMemories();
        });

        calendarCard.setOnClickListener(v -> {
            animateClick(calendarCard);
            navigateToCalendar();
        });

        messagesCard.setOnClickListener(v -> {
            animateClick(messagesCard);
            navigateToMessages();
        });

        managePeopleCard.setOnClickListener(v -> {
            animateClick(managePeopleCard);
            navigateToManagePeople();
        });

        // Events
        viewAllEventsTextView.setOnClickListener(v -> navigateToEvents());

        // Logout button
        logoutButton.setOnClickListener(v -> performLogout());
    }

    private void animateClick(View view) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.95f, 1.0f, 0.95f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(100);
        scaleAnimation.setRepeatCount(1);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        view.startAnimation(scaleAnimation);
    }

    private void showMenuOptions() {
        // Create a popup menu or navigate to settings
        Toast.makeText(this, "Menu Options: Settings, Profile, Help", Toast.LENGTH_SHORT).show();
        // TODO: Implement actual menu popup or navigation
    }

    private void navigateToAddMemory() {
        Toast.makeText(this, "Opening Add Memory...", Toast.LENGTH_SHORT).show();
        // TODO: Navigate to AddMemoryActivity
        // Intent intent = new Intent(this, AddMemoryActivity.class);
        // startActivity(intent);
    }

    private void navigateToViewMemories() {
        Toast.makeText(this, "Opening Memory Timeline...", Toast.LENGTH_SHORT).show();
        // TODO: Navigate to ViewMemoriesActivity
        // Intent intent = new Intent(this, ViewMemoriesActivity.class);
        // startActivity(intent);
    }

    private void navigateToCalendar() {
        Toast.makeText(this, "Opening Calendar...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, CalendarActivity.class);
        startActivity(intent);
    }


    private void navigateToMessages() {
        Toast.makeText(this, "Opening Messages...", Toast.LENGTH_SHORT).show();
        // TODO: Navigate to MessagesActivity
        // Intent intent = new Intent(this, MessagesActivity.class);
        // startActivity(intent);
    }

    private void navigateToManagePeople() {
        Toast.makeText(this, "Opening People Management...", Toast.LENGTH_SHORT).show();
        // TODO: Navigate to ManagePeopleActivity
        // Intent intent = new Intent(this, ManagePeopleActivity.class);
        // startActivity(intent);
    }

    private void navigateToEvents() {
        Toast.makeText(this, "Opening All Events...", Toast.LENGTH_SHORT).show();
        // TODO: Navigate to EventsActivity
        // Intent intent = new Intent(this, EventsActivity.class);
        // startActivity(intent);
    }

    private void performLogout() {
        FirebaseAuth.getInstance().signOut();

        // Clear user preferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        // Navigate to login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
    }

    private void loadProfileImage(String userImagePath, ImageView profileImageView) {
        if (userImagePath != null) {
            File file = new File(userImagePath);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(userImagePath);
                profileImageView.setImageBitmap(bitmap);
            } else {
                // If file doesn't exist, clear SharedPreferences and redirect to SetupActivity
                getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        .edit()
                        .remove("userImagePath")
                        .remove("userName")
                        .apply();
                Toast.makeText(this, "Profile image missing. Please set up your profile again.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, SetupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        } else {
            profileImageView.setImageResource(R.drawable.ic_person);
        }
    }

    // Method to update stats when user performs actions
    public void updateMemoryCount() {
        SharedPreferences prefs = getSharedPreferences("AppStats", MODE_PRIVATE);
        int currentCount = prefs.getInt("memoryCount", 0);
        prefs.edit().putInt("memoryCount", currentCount + 1).apply();
        memoryCountTextView.setText(String.valueOf(currentCount + 1));
    }

    public void updateStreak() {
        SharedPreferences prefs = getSharedPreferences("AppStats", MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastActiveDate = prefs.getString("lastActiveDate", "");

        if (!today.equals(lastActiveDate)) {
            int currentStreak = prefs.getInt("streakCount", 0);
            prefs.edit()
                    .putInt("streakCount", currentStreak + 1)
                    .putString("lastActiveDate", today)
                    .apply();
            streakCountTextView.setText(String.valueOf(currentStreak + 1));
        }
    }

    public void updatePeopleCount(int count) {
        SharedPreferences prefs = getSharedPreferences("AppStats", MODE_PRIVATE);
        prefs.edit().putInt("peopleCount", count).apply();
        peopleCountTextView.setText(String.valueOf(count));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadStatsData();
        updateWelcomeMessage();
    }
}

