package com.chat.service;

import com.chat.model.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public boolean register(User user) {
        return users.putIfAbsent(
                user.getUsername().toLowerCase(),
                user
        ) == null;
    }

    public User findUser(String username) {
        return users.get(username.toLowerCase());
    }

    public boolean usernameExists(String username) {
        return users.containsKey(username.toLowerCase());
    }
}