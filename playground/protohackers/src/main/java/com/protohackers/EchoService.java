package com.protohackers;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoService {
    public static void main(String[] args) {
        int port = 9003; // TODO: this should come from command line args
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Error binding to a port: " + e);
            return;
        }
        Socket socket = null;
        try {
            socket = serverSocket.accept();
        } catch (IOException e) {
            System.out.println("Error creating a socket: " + e);
            return;
        }
        InputStream input = null;
        try {
            input = socket.getInputStream();
        } catch (IOException e) {
            System.out.println("Error opening the input stream: " + e);
            return;
        }
        InputStreamReader reader = new InputStreamReader(input);
        OutputStream output = null;
        try {
            output = socket.getOutputStream();
        } catch (IOException e) {
            System.out.println("Error opening the output stream: " + e);
            return;
        }
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
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing the connection: " + e);
            return;
        }

    }
}