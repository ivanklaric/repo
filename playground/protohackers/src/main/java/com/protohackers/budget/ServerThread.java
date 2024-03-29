package com.protohackers.budget;

import java.io.*;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerThread extends Thread {
    private final Socket socket;
    private String thisUser = "";

    private static final MessageQueue messageQueue = new MessageQueue();
    private static final UserDirectory userDirectory = new UserDirectory();

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    private void processMessageQueue(BufferedWriter writer) throws IOException {
        while (messageQueue.getUnreadMessages(thisUser) > 0) {
            String msg = messageQueue.getMessageAtIndex(messageQueue.getNextMessageIndex(thisUser) );
            String user = messageQueue.getUserAtIndex(messageQueue.getNextMessageIndex(thisUser));
            if (!user.equals(thisUser)) {
                // we don't want to echo our own messages
                sendMessageToClient(writer, msg);
            }
            messageQueue.incrementMessageIndex(thisUser);
        }
    }

    private void sendMessageToClient(BufferedWriter writer, String msg) throws IOException {
        System.out.println("Sent to " + thisUser+ ": " + msg);
        writer.write(msg + "\n");
        writer.flush();
    }

    private boolean isValidUsername(String username) {
        if (username == null || username.isEmpty())
            return false;
        Pattern pattern = Pattern.compile("^[a-z0-9]+$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(username);
        return matcher.find();
    }

    public void run() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            return;
        }
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            return;
        }
        try {
            // first, send intro and ask for a name
            sendMessageToClient(writer, "Welcome to budgetchat! What shall I call you?");
            String name = reader.readLine();
            if (!isValidUsername(name)) {
                writer.close();
                socket.close();
                return;
            }

            thisUser = name;
            // show the user who is in the room
            synchronized(messageQueue) {
                sendMessageToClient(writer, "* The room contains: " + userDirectory.userList());
                userDirectory.registerUser(thisUser);
                messageQueue.addMessage(thisUser, "* " + thisUser + " has entered the room");
            }

            // this is the thread relaying messages from other users
            var messageListeningThread = new Thread(() -> {
                try {
                    while (userDirectory.hasUser(thisUser)) {
                        synchronized(messageQueue) {
                            processMessageQueue(writer);
                        }
                    }
                } catch (IOException e) {
                    // failed to write, might as well give up
                    System.out.println("IOException while processMessageQueue: " + e);
                }
            });
            messageListeningThread.start();

            while (true) {
                // this is the thread reading from the user
                try {
                    String message = reader.readLine();
                    if (message == null) {
                        System.out.println("Client disconnected.");
                        break;
                    }
                    messageQueue.addMessage(thisUser,"[" + thisUser + "] " + message);
                } catch (IOException ioEx) {
                    // client probably disconnected, we better break
                    System.out.println("Error reading from the client: " + ioEx);
                    break;
                }
            }

            // we exited the read loop, so the client obviously disconnected.
            synchronized(messageQueue) {
                userDirectory.removeUser(thisUser);
                if (userDirectory.getUserCount() > 0) {
                    // we're not the last user, announce to others we left. Otherwise, there will be no one to read this.
                    messageQueue.addMessage(thisUser, "* " + thisUser + " has left the room");
                }
            }
            writer.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Unknown IO Exception: " + e);
        }
    }
}