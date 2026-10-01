package com.chat;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

    public ClientHandler getClientByUsername(String username) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equalsIgnoreCase(username)) {
                return client;
            }
        }

        return null;
    }

    public String getOnlineUsers() {
        return clients.stream()
                .map(ClientHandler::getUsername)
                .sorted()
                .collect(Collectors.joining("\n- ", "- ", ""));
    }
}