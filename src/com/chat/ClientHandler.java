package com.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final ChatRoom chatRoom;

    private PrintWriter writer;
    private String username;
    private boolean joined = false;

    public ClientHandler(Socket clientSocket, ChatRoom chatRoom) {
        this.clientSocket = clientSocket;
        this.chatRoom = chatRoom;
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
            username = reader.readLine();

            if (username == null || username.isBlank()) {
                return;
            }

            while(true) {
                if(chatRoom.registerClient(this, username)) {
                    break;
                }

                writer.println("Username already taken. Enter another username:");
                username = reader.readLine();

                if (username == null || username.isBlank()) {
                    return;
                }
            }
            joined = true;
            chatRoom.broadcast(username + " joined the chat.", this);

            String message;
            while ((message = reader.readLine()) != null) {
                if (message.startsWith("/")) {
                    boolean shouldExit = handleCommand(message);

                    if (shouldExit) {
                        break;
                    }
                    continue;
                }

                String formattedMessage = username + ": " + message;

                System.out.println(formattedMessage);
                chatRoom.broadcast(formattedMessage, this);
            }

        } catch (IOException e) {
            System.out.println(username + " disconnected unexpectedly.");

        } finally {
            if (joined) {
                chatRoom.removeClient(this);
                chatRoom.broadcast(username + " left the chat.", this);
            }
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean handleCommand(String message) {
        String[] parts = message.split(" ", 3);
        String command = parts[0].toLowerCase();

        switch (command) {
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
                        "[Private] " + username + " -> " +
                                targetUsername + ": " + privateMessage
                );

                writer.println(
                        "[Private] You -> " +
                                targetUsername + ": " + privateMessage
                );
                break;

            case "/exit":
                writer.println("GoodBye!");
                return true;

            default:
                writer.println("Unknown command: " + command);
                writer.println("Available commands: /users, /pm, /exit");
        }

        return false;
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

    public String getUsername() {
        return username;
    }
}