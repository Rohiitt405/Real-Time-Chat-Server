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

            chatRoom.addClient(this);
            joined = true;
            chatRoom.broadcast(username + " joined the chat.", this);

            String message;
            while ((message = reader.readLine()) != null) {
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }

                String formattedMessage = username + ": " + message;

                System.out.println(formattedMessage);
                chatRoom.broadcast(formattedMessage, this);
            }

        } catch (IOException e) {
            System.out.println(username + " disconnected unexpectedly.");

        } finally {
            if(joined) {
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

    public void sendMessage(String message) {
        writer.println(message);
    }
}
