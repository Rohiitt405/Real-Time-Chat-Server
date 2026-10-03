package com.chat;

import com.chat.model.User;
import com.chat.service.AuthService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final ChatRoom chatRoom;
    private final AuthService authService;

    private PrintWriter writer;
    private User user;
    private boolean joined = false;

    public ClientHandler(
            Socket clientSocket,
            ChatRoom chatRoom,
            AuthService authService
    ) {
        this.clientSocket = clientSocket;
        this.chatRoom = chatRoom;
        this.authService = authService;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream())
                )
        ) {
            writer = new PrintWriter(clientSocket.getOutputStream(), true);

            if (!authenticate(reader)) {
                return;
            }

            chatRoom.addClient(this);
            joined = true;

            writer.println("Authentication successful.");
            writer.println("You can now start chatting.");
            writer.println("Type 'exit' to leave.");

            chatRoom.broadcast(user.getDisplayName() + " joined the chat.", this);

            String message;
            while ((message = reader.readLine()) != null) {
                if (message.startsWith("/")) {
                    boolean shouldExit = handleCommand(message);

                    if (shouldExit) {
                        break;
                    }
                    continue;
                }

                String formattedMessage = user.getDisplayName() + ": " + message;

                System.out.println(formattedMessage);
                chatRoom.broadcast(formattedMessage, this);
            }

        } catch (IOException e) {
            System.out.println(
                    user != null
                            ? user.getUsername() + " disconnected unexpectedly."
                            : "Client disconnected unexpectedly."
            );

        } finally {
            if (joined) {
                chatRoom.removeClient(this);
                chatRoom.broadcast(user.getDisplayName() + " left the chat.", this
                );
            }
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean authenticate(BufferedReader reader) throws IOException {
        while (true) {
            writer.println("Choose an option:");
            writer.println("1. Register");
            writer.println("2. Login");
            writer.println("Enter your choice:");

            String choice = reader.readLine();

            if (choice == null) {
                return false;
            }

            switch (choice.trim()) {
                case "1":
                    if (register(reader)) {
                        return true;
                    }
                    break;
                case "2":
                    if (login(reader)) {
                        return true;
                    }
                    break;

                default:
                    writer.println("Invalid choice.");
            }
        }
    }

    private boolean register(BufferedReader reader) throws IOException {
        writer.println("Enter your username:");
        String username = reader.readLine();

        if (username == null || username.isBlank()) {
            return false;
        }

        if (!username.matches("[a-zA-Z0-9_]+")) {
            writer.println("Username can only contain letter, number and underscore.");
            return false;
        }

        writer.println("Enter your display name:");
        String displayName = reader.readLine();

        if(displayName == null || displayName.isBlank()) {
            writer.println("Diplay name cannot be empty.");
            return false;
        }

        writer.println("Enter your password:");
        String password = reader.readLine();

        if(password == null || password.isBlank()) {
            writer.println("Password cannot be empty.");
            return false;
        }

        boolean registered = authService.register(
                username,
                displayName,
                password
        );

        if(!registered) {
            writer.println("Username already taken.");
            return false;
        }

        user = authService.login(username, password);
        return user != null;
    }

    private boolean login(BufferedReader reader) throws IOException {
        writer.println("Enter your username:");
        String username = reader.readLine();

        if(username == null || username.isBlank()) {
            return false;
        }

        writer.println("Enter your password:");
        String password = reader.readLine();

        if(password == null || password.isBlank()) {
            return false;
        }

        user = authService.login(username, password);
        if(user == null) {
            writer.println("Invalid username and password.");
            return false;
        }
        return true;
    }

    private boolean handleCommand(String message) {
        String[] parts = message.trim().split("\\s+", 3);
        String command = parts[0].toLowerCase();

        switch (command) {
            case "/help":
                writer.println("Available commands:");
                writer.println("/help  - Show available commands");
                writer.println("/users  - Show online users");
                writer.println("/pm username message  - Send a private message");
                writer.println("/exit  - Leave the chat");
                break;

            case "/users":
                writer.println("Online users:");
                writer.println(chatRoom.getOnlineUsers());
                break;

            case "/pm":
                if (parts.length < 3) {
                    writer.println("Usage: /pm username message");
                    break;
                }

                String targetUsername = parts[1];
                String privateMessage = parts[2];

                ClientHandler target = chatRoom.getClientByUsername(targetUsername);

                if (target == null) {
                    writer.println("User '" + targetUsername + "' is not online.");
                    break;
                }

                target.sendMessage(
                        "[Private] " + user.getDisplayName() +
                                " -> " + target.getDisplayName() +
                                ": " + privateMessage
                );

                writer.println(
                        "[Private] You -> " + target.getDisplayName() +
                                ": " + privateMessage
                );
                break;

            case "/exit":
                writer.println("GoodBye!");
                return true;

            default:
                writer.println("Unknown command: " + command);
                writer.println("Use /help to see available commands.");
        }

        return false;
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

    public String getUsername() {
        return user.getUsername();
    }

    public String getDisplayName() {
        return user.getDisplayName();
    }
}