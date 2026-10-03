package com.chat.model;

public class User {
    private final String username;
    private final String displayName;
    private final String passwordHash;

    public User(
            String username,
            String displayName,
            String passwordHash
    ) {
        this.username = username;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}