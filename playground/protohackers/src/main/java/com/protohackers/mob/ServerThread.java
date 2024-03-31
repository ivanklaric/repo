package com.protohackers.mob;

import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread {

    private final Socket clientSocket;
    private Socket serverSocket;

    static class ThreadSynchronization {
        private boolean serverThreadFinished = false;
        private boolean clientThreadFinished = false;
        private final Object lock = new Object();

        public void waitForAny() throws InterruptedException {
            synchronized (lock) {
                while (!serverThreadFinished && !clientThreadFinished) {
                    lock.wait();
                }
            }
        }

        public void notifyFinished(int threadId) {
            synchronized (lock) {
                if (threadId == 1) {
                    serverThreadFinished = true;
                } else {
                    clientThreadFinished = true;
                }
                lock.notify();
            }
        }
    }

    public ServerThread(Socket socket) {
        this.clientSocket = socket;

        // connect to the server
        String serverAddr = "chat.protohackers.com";
        int serverPort = 16963;
        try {
            this.serverSocket = new Socket(serverAddr, serverPort);
        } catch (IOException e) {
            this.serverSocket = null;
            System.out.println("Error connecting to " + serverAddr +": " + e);
        }
    }

    private String readLine(BufferedReader reader) {
        StringBuilder builder = new StringBuilder();
        int charValue;
        while (true) {
            try {
                if ((charValue = reader.read()) == -1) break;
            } catch (IOException e) {
                break;
            }
            if ((char)charValue == '\n') {
                return builder.toString();
            }
            builder.append((char)charValue);
        }
        return null;
    }

    private Thread createListenerThread(BufferedReader reader,
                                        BufferedWriter writer,
                                        ThreadSynchronization syncMechanism,
                                        int threadId) {
        return new Thread(() -> {
            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
                // this is the thread reading from the user and writing to the server
                try {
                    String message = readLine(reader);
                    if (message == null) {
                        System.out.println("Client disconnected.");
                        break;
                    }
                    String rewrittenMessage = MessageRewriter.rewriteMessage(message);
                    System.out.println("Got: " + message);
                    System.out.println("Sending: " + rewrittenMessage);
                    writer.write(MessageRewriter.rewriteMessage(message) + "\n");
                    writer.flush();
                } catch (IOException ioEx) {
                    // client probably disconnected, we better break
                    System.out.println("Error reading from the client: " + ioEx);
                    break;
                }
            }
            syncMechanism.notifyFinished(threadId);
        });
    }

    public void run() {
        // Initialization failed in the constructor, die immediately;
        if (this.serverSocket == null) {
            System.out.println("No server socket available, exiting thread.");
            return;
        }

        // Prepare client readers and writers
        BufferedReader clientReader;
        try {
            clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Can't open client Input stream: " + e);
            return;
        }
        BufferedWriter clientWriter;
        try {
            clientWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            System.out.println("Can't open client Writer stream: " + e);
            return;
        }

        // Prepare server reader or writers
        BufferedReader serverReader;
        try {
            serverReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Can't open server Input stream: " + e);
            return;
        }
        BufferedWriter serverWriter;
        try {
            serverWriter = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()));
        } catch (IOException e) {
            System.out.println("Can't open server Writer stream: " + e);
            return;
        }

        var syncMechanism = new ThreadSynchronization();
        var clientListeningThread = createListenerThread(clientReader, serverWriter, syncMechanism,1 );
        clientListeningThread.start();

        var serverListeningThread = createListenerThread(serverReader, clientWriter, syncMechanism,2);
        serverListeningThread.start();

        try {
            syncMechanism.waitForAny();
        } catch (InterruptedException e) {
            serverListeningThread.interrupt();
            clientListeningThread.interrupt();
        }

        try {
            clientWriter.close();
            clientReader.close();
            clientSocket.close();
            serverWriter.close();
            serverReader.close();
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing reader, writer, or socket: " + e);
        }
    }

}

