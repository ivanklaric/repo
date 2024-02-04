package com.protohackers.echo;

import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread {
    private final Socket socket;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    private DataInputStream openInputStream() {
        DataInputStream input;
        try {
            input = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("Error opening input stream: " + e);
            return null;
        }
        return input;
    }

    private DataOutputStream openOutputStream() {
        DataOutputStream output;
        try {
            output = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Error opening the output stream: " + e);
            return null;
        }
        return output;
    }

    public void run() {
        InputStream input = openInputStream();
        OutputStream output = openOutputStream();
        if (input == null || output == null) {
            System.out.println("Couldn't open I/O streams, exiting.");
            return;
        }

        int ch;
        try {
            while ((ch = input.read()) >= 0) {
                output.write(ch);
            }
        } catch (IOException e) {
            System.out.println("Error doing I/O: " + e);
        }
        try {
            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e);
        }
    }
}
