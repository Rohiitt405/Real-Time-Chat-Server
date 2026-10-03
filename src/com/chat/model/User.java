package com.chat.model;

public class User {
    private final String username;
    private final String displayName;

    public User(String username, String displayName) {
        this.username = username;
        this.displayName = displayName;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }
}