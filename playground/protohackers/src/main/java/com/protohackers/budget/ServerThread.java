package com.protohackers.budget;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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

    private String readLine(Reader reader) throws IOException {
        var ret = new StringBuilder();
        char ch = ' ';
        while (ch != '\n') {
            int charRead = reader.read();
            if (charRead == -1) {
                return null;
            }
            ch = (char) charRead;
            if (ch == '\r') {
                System.out.println("CR found!");
            }
            if (ch != '\n')
                ret.append(ch);
        }
        return ret.toString();
    }

    private void processMessageQueue(BufferedWriter writer) throws IOException {
        while (messageQueue.getUnreadMessages(thisUser) > 0) {
            String msg = messageQueue.getMessageAtIndex(messageQueue.getNextMessageIndex(thisUser) );
            // we don't want to echo our own messages
            if (!msg.startsWith("[" + thisUser + "]") && !msg.startsWith("* " + thisUser + " has")) {
                sendMessageToClient(writer, msg);
            }
            messageQueue.incrementMessageIndex(thisUser);
        }
    }

    private void sendMessageToClient(BufferedWriter writer, String msg) throws IOException {
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
            sendMessageToClient(writer, "* The room contains: " + userDirectory.userList());
            userDirectory.registerUser(thisUser);
            messageQueue.addMessage("* " + thisUser + " has entered the room");
            while (true) {
                try {
                    socket.setSoTimeout(100);
                } catch (SocketException e) {
                    System.out.println("SocketException when setSoTimeout(), dying.");
                    return; // there's an error in the underlying protocol, we better die.
                }
                try {
                    String message = reader.readLine();
                    if (message == null) {
                        System.out.println("Client disconnected.");
                        break;
                    }
                    messageQueue.addMessage("[" + thisUser + "] " + message);
                } catch (SocketTimeoutException timeoutEx) {
                    // timeout, see if there are messages in the queue to show
                    processMessageQueue(writer);
                } catch (IOException ioEx) {
                    // client probably disconnected, we better break
                    System.out.println("Client disconnected.");
                    break;
                }
            }
            userDirectory.removeUser(thisUser);
            if (userDirectory.getUserCount() > 0) {
                // we're not the last user, announce to others we left. Otherwise there will be noone to read this.
                messageQueue.addMessage("* " + thisUser + " has left the room");
            }
            writer.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Unknown IO Exception: " + e);
        }
    }
}