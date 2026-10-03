package com.chat.service;

import com.chat.model.User;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {
    private final UserService userService;

    public AuthService(UserService userService) {
        this.userService = userService;
    }

    public boolean register(
            String username,
            String displayName,
            String password
    ) {
        if(userService.usernameExists(username)) {
            return false;
        }

        String passwordHash = BCrypt.hashpw(
                password,
                BCrypt.gensalt()
        );

        User user = new User(
                username,
                displayName,
                passwordHash
        );
        return userService.register(user);
    }

    public User login(String username, String password) {
        User user = userService.findUser(username);

        if(user == null) {
            return null;
        }

        boolean passwordMatches = BCrypt.checkpw(
                password,
                user.getPasswordHash()
        );

        if(!passwordMatches) {
            return null;
        }
        return user;
    }
}
