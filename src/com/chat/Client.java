package com.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    public static void main(String[] args) {
        try (
                Socket socket = new Socket(HOST, PORT);
                BufferedReader serverReader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );

                PrintWriter writer = new PrintWriter(
                        socket.getOutputStream(),
                        true
                );

                BufferedReader consoleReader = new BufferedReader(
                        new InputStreamReader(System.in)
                )
        ) {

            System.out.println("Connected to server!");

            String usernamePrompt = serverReader.readLine();
            System.out.println(usernamePrompt);

            String username = consoleReader.readLine();
            writer.println(username);

            System.out.println("You can now start chatting.");
            System.out.println("Type 'exit' to leave.");

            Thread receiverThread = new Thread(() -> {

                try {
                    String message;

                    while ((message = serverReader.readLine()) != null) {
                        System.out.println("\n" + message);
                        System.out.print("> ");
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            });

            receiverThread.start();

            String message;

            while ((message = consoleReader.readLine()) != null) {
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }

                writer.println(message);
            }

        } catch (IOException e) {
            System.out.println("Connection closed.");
            e.printStackTrace();
        }
    }
}