package com.example.namedmemories.models;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class User {
    public String name;
    public String profileImageUrl;
    public String joinDate;
    public String userId;
    public String email;
    public String phone;
    public String bio;

    @ServerTimestamp
    public Date createdAt;

    @ServerTimestamp
    public Date updatedAt;

    // Required empty constructor for Firebase
    public User() {}

    // Constructor with basic fields
    public User(String name, String profileImageUrl, String joinDate) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.joinDate = joinDate;
    }

    // Constructor with all fields
    public User(String name, String profileImageUrl, String joinDate, String userId, String email) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.joinDate = joinDate;
        this.userId = userId;
        this.email = email;
    }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("profileImageUrl", profileImageUrl);
        map.put("joinDate", joinDate);
        map.put("userId", userId);
        map.put("email", email);
        map.put("phone", phone);
        map.put("bio", bio);
        map.put("updatedAt", FieldValue.serverTimestamp());

        // Only add createdAt for new users
        if (createdAt == null) {
            map.put("createdAt", FieldValue.serverTimestamp());
        }

        return map;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getJoinDate() { return joinDate; }
    public void setJoinDate(String joinDate) { this.joinDate = joinDate; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}