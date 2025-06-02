package com.example.namedmemories.models;

public class User {
    public String name;
    public String profileImageUrl;
    public String joinDate;

    public User() {
        // Required empty constructor for Firebase
    }

    public User(String name, String profileImageUrl, String joinDate) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.joinDate = joinDate;
    }
}
