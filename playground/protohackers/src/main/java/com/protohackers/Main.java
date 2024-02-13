package com.protohackers;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Main {

    public static Thread buildNewServerThread(String serviceName, Socket socket) {
        if (serviceName.equals("echo")) {
            return new com.protohackers.echo.ServerThread(socket);
        }
        if (serviceName.equals("prime")) {
            return new com.protohackers.prime.ServerThread(socket);
        }
        return null;
    }

    public static void main(String[] args) {
        int port = 9007; // TODO: this should come from command line args

        String[] listOfAcceptableArgs = {"echo", "prime"};
        Set<String> acceptableArgs = new HashSet<>(Arrays.asList(listOfAcceptableArgs));
        if (args.length < 1 || !acceptableArgs.contains(args[0])) {
            System.out.println("Args:");
            System.out.println("echo  - for EchoService");
            System.out.println("prime - for PrimeTime");
            return;
        }
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    var socket = serverSocket.accept();
                    var thread = buildNewServerThread(args[0], socket);
                    if (thread != null)
                        thread.start();
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