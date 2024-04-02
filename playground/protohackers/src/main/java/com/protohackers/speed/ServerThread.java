package com.protohackers.speed;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class ServerThread extends Thread {
    public enum ThreadMode {
        UNKNOWN, CAMERA, DISPATCHER
    }
    private static final CarObservatory carObservatory = new CarObservatory();
    private Message cameraContext = null;
    private Message dispatcherContext = null;

    private static final Map<Long, Semaphore> dispatcherSemaphores = new ConcurrentHashMap<>();
    private static final Map<Long, List<Message>> ticketsToDispatch = new ConcurrentHashMap<>();

    private final Socket clientSocket;
    private ThreadMode threadMode = ThreadMode.UNKNOWN;
    private boolean wantHeartbeat = false;
    private long heartbeatInterval;
    final Object syncObj = new Object();


    private synchronized void addCamera(Message msg) {
        if (msg.getType() != Message.MessageType.I_AM_CAMERA) return;
        cameraContext = msg;
    }

    private synchronized void addDispatcher(Message msg) {
        if (msg.getType() != Message.MessageType.I_AM_DISPATCHER) return;
        dispatcherContext = msg;
        synchronized (dispatcherSemaphores) {
            for (var road : dispatcherContext.getDispatcherRoads()) {
                dispatcherSemaphores.computeIfAbsent(road, k -> new Semaphore(1));
            }
        }
    }

    private synchronized void addCarObservation(Message msg) {
        if (msg.getType() != Message.MessageType.PLATE || cameraContext == null) return;
        carObservatory.addCarSighting(msg.getPlate(), msg.getTimestamp(),
                cameraContext.getRoad(), cameraContext.getMile(), cameraContext.getLimit());
        var ticketsToIssue = carObservatory.issueTickets();
        for (var ticket : ticketsToIssue) {
            try {
                dispatcherSemaphores.get(ticket.getRoad()).acquire();
                ticketsToDispatch.computeIfAbsent(ticket.getRoad(), k -> new ArrayList<>());
                ticketsToDispatch.get(ticket.getRoad()).add(ticket);
            } catch (InterruptedException e) {
                break;
            } finally {
                dispatcherSemaphores.get(ticket.getRoad()).release();
            }
        }
    }

    public ServerThread(Socket socket) {
        this.clientSocket = socket;
    }

    private void writeToClient(OutputStream outputStream, Message msg) throws IOException {
        synchronized (syncObj) {
            MessageIO.writeMessage(outputStream, msg);
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
                    for (var road : dispatcherContext.getDispatcherRoads()) {
                        Thread.ofVirtual().start( () -> {
                            while (true) {
                                try {
                                    dispatcherSemaphores.get(road).acquire();
                                    if (ticketsToDispatch.containsKey(road)) {
                                        for (Message ticket : ticketsToDispatch.get(road)) {
                                            try {
                                                writeToClient(outputStream, ticket);
                                            } catch (IOException e) {
                                                return;
                                            }
                                        }
                                    }
                                    ticketsToDispatch.get(road).clear();
                                } catch (InterruptedException e) {
                                    return;
                                } finally {
                                    dispatcherSemaphores.get(road).release();
                                }
                            }
                        });
                    }
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
