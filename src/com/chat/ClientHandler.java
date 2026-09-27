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

    public ClientHandler(Socket clientSocket, ChatRoom chatRoom) {
        this.clientSocket = clientSocket;
        this.chatRoom = chatRoom;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream())
                );
        ) {
            writer = new PrintWriter(clientSocket.getOutputStream(), true);

            chatRoom.addClient(this);

            String message;

            while((message = reader.readLine()) != null) {
                System.out.println("Message: " + message);
                chatRoom.broadcast(message, this);
            }

        } catch (IOException e) {
            System.out.println("Client disconnected.");

        } finally {
            chatRoom.removeClient(this);

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
