package com.chat;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoom {
    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    public void addClient(ClientHandler client) {
        clients.add(client);
        System.out.println("Client joined. Total Clients: " + clients.size());
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Client left. Total Clients: " + clients.size());
    }

    public void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }
}
