package com.protohackers;

import com.protohackers.unusual.UnusualClient;
import com.protohackers.unusual.UnusualMessage;

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

    public static void runTcpApps(String appName, int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    var socket = serverSocket.accept();
                    var thread = buildNewServerThread(appName, socket);
                    if (thread != null)
                        thread.start();
                    System.out.println("Listening on " + port);
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
        try {
            var socket = new DatagramSocket(port);
            var thread = new com.protohackers.unusual.ServerThread(socket);
            thread.start();
            System.out.println("Listening on " + port);
        } catch (SocketException e) {
            System.out.println("Error when creating a UDP socket: " + e);
        }
    }

    public static void main(String[] args) {
        int port = 9003; // TODO: this should come from command line args

        String[] listOfAcceptableArgs = {"echo", "prime", "means", "budget", "unusual-server", "unusual-client"};
        Set<String> acceptableArgs = new HashSet<>(Arrays.asList(listOfAcceptableArgs));
        if (args.length < 1 || !acceptableArgs.contains(args[0])) {
            System.out.println("Args:");
            System.out.println("echo  - for EchoService");
            System.out.println("prime - for PrimeTime");
            System.out.println("means - for MeansToAnEnd");
            System.out.println("budget - for BudgetChat");
            System.out.println("unusual-server - for UnusualDatabaseProgram server");
            System.out.println("unusual-client - for UnusualDatabaseProgram client");
            return;
        }
        String appName = args[0];
        if (!appName.startsWith("unusual")) {
            runTcpApps(appName, port);
        } else {
            if (appName.equals("unusual-server"))
                runUdpApps(appName, port);
            if (appName.equals("unusual-client")) {
                if (args.length < 4) {
                    System.out.println("unusual-client <server> <port> <message>");
                    return;
                }
                String server = args[1];
                int serverPort = Integer.parseInt(args[2]);
                String msg = args[3];
                System.out.println("Sending message " + msg + " to " + server + ":" + serverPort + ".");
                UnusualClient.sendMessage(server, serverPort, new UnusualMessage(msg));
            }

        }
    }

}
