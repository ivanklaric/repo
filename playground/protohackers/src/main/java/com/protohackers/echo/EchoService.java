package com.protohackers.echo;

import java.io.*;
import java.net.ServerSocket;

public class EchoService {
    public static void main(String[] args) {
        int port = 9003; // TODO: this should come from command line args

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    new ServerThread(serverSocket.accept()).start();
                } catch (IOException e) {
                    System.out.println("Error creating a socket: " + e);
                    return;
                }
            }
        } catch (IOException e) {
            System.out.println("Error when opening server: " + e);
        }
    }
}