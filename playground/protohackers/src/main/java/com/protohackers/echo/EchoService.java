package com.protohackers.echo;

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

        while (true) {
            try {
                new ServerThread(serverSocket.accept()).start();
            } catch (IOException e) {
                System.out.println("Error creating a socket: " + e);
                return;
            }
        }
    }
}