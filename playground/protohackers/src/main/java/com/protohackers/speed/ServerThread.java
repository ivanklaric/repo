package com.protohackers.speed;

import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread {
    public enum ThreadMode {
        UNKNOWN, CAMERA, DISPATCHER
    }
    private static final CarObservatory carObservatory = new CarObservatory();
    private Message cameraContext = null;
    private Message dispatcherContext = null;

    private final Socket clientSocket;
    private ThreadMode threadMode = ThreadMode.UNKNOWN;
    private boolean wantHeartbeat = false;
    private long heartbeatInterval;
    Object syncObj = new Object();


    private synchronized void addCamera(Message msg) {
        if (msg.getType() != Message.MessageType.I_AM_CAMERA) return;
        cameraContext = msg;
    }

    private synchronized void addDispatcher(Message msg) {
        if (msg.getType() != Message.MessageType.I_AM_DISPATCHER) return;
        dispatcherContext = msg;
        // todo update some probably useful road: dispatcher mappings
    }

    private synchronized void addCarObservation(Message msg) {
        if (msg.getType() != Message.MessageType.PLATE || cameraContext == null) return;
        carObservatory.addCarSighting(msg.getPlate(), msg.getTimestamp(),
                cameraContext.getRoad(), cameraContext.getMile(), cameraContext.getLimit());
    }

    public ServerThread(Socket socket) {
        this.clientSocket = socket;
    }

    private void writeToClient(OutputStream outputStream, Message msg) throws IOException {
        synchronized (syncObj) {

        }
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
                    writeToClient(outputStream, MessageIO.createErrorMessage("Invalid message sent"));
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
                                    writeToClient(outputStream, MessageIO.createHeartBeatMessage());
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
                    addCamera(msg);
                }
                case Message.MessageType.I_AM_DISPATCHER -> {
                    threadMode = ThreadMode.DISPATCHER;
                    addDispatcher(msg);
                }
                case Message.MessageType.PLATE ->
                    addCarObservation(msg);
            }
        }
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing the socket: "+ e);
        }
    }
}
