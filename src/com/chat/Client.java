package com.chat;

import java.io.IOException;
import java.net.Socket;

public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT)) {

            System.out.println("Connected to server!");

        } catch (IOException e) {

            System.out.println("Could not connect to server.");
            e.printStackTrace();
        }
    }
}
