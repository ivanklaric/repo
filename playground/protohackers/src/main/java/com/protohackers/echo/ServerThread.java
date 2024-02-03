package com.protohackers.echo;

import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread {
    private final Socket socket;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    private InputStream openInputStream() {
        InputStream input;
        try {
            input = socket.getInputStream();
        } catch (IOException e) {
            System.out.println("Error opening input stream: " + e);
            return null;
        }
        return input;
    }

    private OutputStream openOutputStream() {
        OutputStream output;
        try {
            output = socket.getOutputStream();
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

        InputStreamReader reader = new InputStreamReader(input);
        OutputStreamWriter writer = new OutputStreamWriter(output);
        int ch;
        try {
            while ((ch = reader.read()) >= 0) {
                writer.write(ch);
            }
        } catch (IOException e) {
            System.out.println("Error doing I/O: " + e);
        }
        try {
            writer.close();
            reader.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e);
        }
    }
}
