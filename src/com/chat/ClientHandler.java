package com.chat;

import com.chat.model.User;
import com.chat.service.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final ChatRoom chatRoom;
    private final UserService userService;

    private PrintWriter writer;
    private User user;
    private boolean joined = false;

    public ClientHandler(
            Socket clientSocket,
            ChatRoom chatRoom,
            UserService userService
    ) {
        this.clientSocket = clientSocket;
        this.chatRoom = chatRoom;
        this.userService = userService;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream())
                )
        ) {
            writer = new PrintWriter(clientSocket.getOutputStream(), true);
            writer.println("Enter your username:");

            String username;
            while (true) {
                username = reader.readLine();

                if (username == null || username.isBlank()) {
                    return;
                }

                if (!username.matches("[a-zA-Z0-9_]+")) {
                    writer.println("Username can only contain letters, numbers and underscore.");
                    writer.println("Enter your username:");
                    continue;
                }

                break;
            }

            writer.println("Enter your display name:");
            String displayName = reader.readLine();

            if (displayName == null || displayName.isBlank()) {
                return;
            }

            user = new User(username, displayName);

            while (!userService.register(user)) {
                writer.println("Username already taken. Enter another username:");
                username = reader.readLine();

                if (username == null || username.isBlank()) {
                    return;
                }
                if (!username.matches("[a-zA-Z0-9_]+")) {
                    writer.println("Username can only contain letters, numbers and underscore.");
                    writer.println("Enter your username:");
                    continue;
                }
                user = new User(username, displayName);
            }

            chatRoom.addClient(this);
            joined = true;

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