package com.hendrychjan;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private final int PORT = 56789;
    private Set<String> users = new HashSet<>(); // set of usernames
    private Set<UserThread> clientWorkers = new HashSet<>(); // set of threads belonging to each user

    // Server entry point
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    public Server() {}

    // Start and init the server
    public void start() {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("Server server running at " + PORT);

            // Wait for new connections
            while (true) {
                // New user connected
                // Establish socket tunnel
                Socket socket = serverSocket.accept();
                System.out.println("New user joined the server");

                // Create new socket worker and add that worker to the Set of active workers
                UserThread newUser = new UserThread(socket, this);
                clientWorkers.add(newUser);
                newUser.start(); // Start the newly created worker
            }

        } catch (IOException ex) {
            System.out.println("Server error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Broadcast message to all connected users
    public void broadcast(String message, UserThread sender) {
        for (UserThread worker : clientWorkers) {
            // Broadcast the message to everyone
            if (worker != sender) { // ...except the one who sent it
                worker.sendMessage(message);
            }
        }
    }

    // Add newly connected user to the set of connected users
    public void addUserName(String userName) {
        users.add(userName);
    }

    // When user disconnects, remove it from the set of disconnected users
    void removeUser(String user, UserThread worker) {
        users.remove(user);
        clientWorkers.remove(worker);
        System.out.println("User " + user + " left.");
    }

    // Get all connected users
    String getUserNames() {
        StringBuilder list = new StringBuilder();
        list.append("[SERVER]: Online users are (");
        for (String user : users) {
            list.append((" " + user + " "));
        }
        list.append(")");
        return list.toString();
    }

    // Check if there are any users on the server
    boolean hasUsers() {
        return !this.users.isEmpty();
    }
}