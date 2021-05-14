package com.hendrychjan;

import java.io.*;
import java.net.*;

public class UserThread extends Thread {
    private Socket socket;
    private Server server;
    private PrintWriter writer;

    public UserThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        String userName = "/unknown/";
        try {
            // Reader to get data from client
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // PrintWriter for sending data to the client
            writer = new PrintWriter(socket.getOutputStream(), true);

            // Tell the user who is currently online
            listLoggedUsers();

            // And print info about the server commands
            StringBuilder info = new StringBuilder();
            info.append('\n' + "[SERVER]: Available commands are:");
            info.append('\n' + ":online: -> to list online users");
            info.append('\n' + ":leave: -> to close the socket connection");

            writer.println(info.toString());

            // NOTE: first thing that new user sends is his userName
            userName = reader.readLine(); // get the username
            server.addUserName(userName); // save it to the list of online users
            // Broadcast the message about newly connected user to everyone
            server.broadcast("[SERVER]: New user connected: " + userName, this);

            // Broadcast the new message to everyone
            String rawMessage = "";
            try {
                boolean keepSession = true;
                do {
                    rawMessage = reader.readLine();

                    switch (rawMessage) {
                        case ":online:":
                            listLoggedUsers();
                            break;
                        case ":leave:":
                            keepSession = false;
                            break;
                        default:
                            String message = "[" + userName + "]: " + rawMessage;
                            server.broadcast(message, this);
                            break;
                    }


                } while (keepSession);
            } catch (Exception e) {
                System.out.println("Unexpectedly disconnected.");
            } finally {
                quit(userName);
            }
        } catch (IOException ex) {
            // Error with the socket connection - terminate
            try {
                quit(userName);
            } catch (IOException e) {
                System.out.println("Server error.");
                e.printStackTrace();
            }
        }
    }

    private void quit(String user) throws IOException {
        server.broadcast("[SERVER]: User " + user + " disconnected...", this);
        server.removeUser(user, this);
        socket.close();
    }

    // Sends a list of all users who are currently online
    void listLoggedUsers() {
        if (server.hasUsers()) {
            writer.println(server.getUserNames());
        } else {
            writer.println("[SERVER]: There is no one else online.");
        }
    }

    void sendMessage(String message) {
        writer.println(message);
    }
}