package com.protohackers;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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
        if (serviceName.equals("means")) {
            return new com.protohackers.means.ServerThread(socket);
        }
        if (serviceName.equals("budget")) {
            return new com.protohackers.budget.ServerThread(socket);

        }
        return null;
    }

    public static void runSocketApps(String appName, int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    var socket = serverSocket.accept();
                    var thread = buildNewServerThread(appName, socket);
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

    public static void runUdpApps(String appName, int port) {
        if (!appName.equals("unusual-server"))
            return;
        while (true) {
            try {
                var socket = new DatagramSocket(port);
                var thread = new com.protohackers.unusual.ServerThread(socket);
                thread.start();
            } catch (SocketException e) {
                System.out.println("Error when creating a socket: " + e);
                return;
            }
        }
    }

    public static void main(String[] args) {
        int port = 9003; // TODO: this should come from command line args

        System.out.println("Listening on " + port);
        String[] listOfAcceptableArgs = {"echo", "prime", "means", "budget", "unusual-server", "unusual-client"};
        Set<String> acceptableArgs = new HashSet<>(Arrays.asList(listOfAcceptableArgs));
        String appName = args[0];
        if (args.length < 1 || !acceptableArgs.contains(appName)) {
            System.out.println("Args:");
            System.out.println("echo  - for EchoService");
            System.out.println("prime - for PrimeTime");
            System.out.println("means - for MeansToAnEnd");
            System.out.println("budget - for BudgetChat");
            System.out.println("unusual-server - for UnusualDatabaseProgram server");
            System.out.println("unusual-client - for UnusualDatabaseProgram client");
            return;
        }
        if (!appName.equals("unusual")) {
            runSocketApps(appName, port);
        } else {
            runUdpApps(appName, port);
        }
    }

}
