package com.protohackers.speed;

import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread {
    public enum ThreadMode {
        UNKNOWN, CAMERA, DISPATCHER
    }
    private final Socket clientSocket;
    private ThreadMode threadMode = ThreadMode.UNKNOWN;
    private boolean wantHeartbeat = false;
    private long heartbeatInterval;
    private long nextHeartbeat;

    public ServerThread(Socket socket) {
        this.clientSocket = socket;
    }
    public void run() {
        InputStream inputStream;
        try {
            inputStream = clientSocket.getInputStream();
        } catch (IOException e) {
            System.out.println("Can't open client Input stream: " + e);
            return;
        }
        OutputStream outputStream;
        try {
            outputStream = clientSocket.getOutputStream();
        } catch (IOException e) {
            System.out.println("Can't open client Output stream: " + e);
            return;
        }

        while (true) {
            var msg = MessageIO.readMessage(inputStream);
            if (!MessageValidator.isClientMessageValid(msg, threadMode, wantHeartbeat)) {
                System.out.println("Read unexpected message, sending error and closing");
                try {
                    MessageIO.writeMessage(outputStream, MessageIO.createErrorMessage("Invalid message sent"));
                } catch (IOException e) {
                    System.out.println("Error writing message to output stream: " + e);
                    break;
                }
                break;
            }

            switch(msg.getType()) {
                case Message.MessageType.WANT_HEARTBEAT -> {
                    if (msg.getInterval() > 0) {
                        wantHeartbeat = true;
                        heartbeatInterval = msg.getInterval();
                        Thread t = new Thread(() -> {
                            while (true) {
                                try {
                                    Thread.sleep(heartbeatInterval * 100);
                                } catch (InterruptedException e) {
                                    break;
                                }
                                try {
                                    MessageIO.writeMessage(outputStream, MessageIO.createHeartBeatMessage());
                                } catch (IOException e) {
                                    break;
                                }
                            }
                        });
                        t.start();
                    }
                }
                case Message.MessageType.I_AM_CAMERA -> {
                    threadMode = ThreadMode.CAMERA;

                }
            }
        }
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing the socket: "+ e);
        }
    }
}
