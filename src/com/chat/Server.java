package com.chat;

import com.chat.service.UserService;
import com.chat.service.AuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 5000;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("Chat server started...");
            System.out.println("Listening on port: " + PORT);

            ChatRoom chatRoom = new ChatRoom();
            UserService userService = new UserService();
            AuthService authService = new AuthService(userService);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected!");

                ClientHandler clientHandler = new ClientHandler(clientSocket, chatRoom, authService);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }

        } catch (IOException e) {
            System.out.println("Server error.");
            e.printStackTrace();
        }
    }
}