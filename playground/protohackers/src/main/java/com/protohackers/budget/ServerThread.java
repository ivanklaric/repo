package com.protohackers.budget;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

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
            ch = (char) reader.read();
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
            if (name != null) {
                thisUser = name;
                // show the user who is in the room
                sendMessageToClient(writer, "* The room contains: " + userDirectory.userList());
                userDirectory.registerUser(thisUser);
                messageQueue.addMessage("* " + thisUser + " has entered the room");
            }
            while (thisUser != null) {
                try {
                    socket.setSoTimeout(500);
                } catch (SocketException e) {
                    return; // there's an error in the underlying protocol, we better die.
                }
                try {
                    String message = readLine(reader); //reader.readLine();
                    messageQueue.addMessage("[" + thisUser + "] " + message);
                } catch (SocketTimeoutException timeoutEx) {
                    processMessageQueue(writer);
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
            // do nothing
        }
    }
}