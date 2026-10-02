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

            String message;

            while ((message = serverReader.readLine()) != null) {
                System.out.println(message);

                if (message.equals("Enter your username:")) {
                    String username = consoleReader.readLine();
                    writer.println(username);
                    continue;
                }

                if (message.equals("Enter your display name:")) {
                    String displayName = consoleReader.readLine();
                    writer.println(displayName);
                    continue;
                }

                if (message.equals("You can now start chatting.")) {
                    break;
                }
            }

            Thread receiverThread = new Thread(() -> {
                try {
                    String serverMessage;

                    while ((serverMessage = serverReader.readLine()) != null) {
                        System.out.println("\n" + serverMessage);
                        System.out.print("> ");
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            });

            receiverThread.start();

            while ((message = consoleReader.readLine()) != null) {
                if (message.equalsIgnoreCase("exit")) {
                    writer.println("/exit");
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