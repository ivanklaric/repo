package com.protohackers.speed;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
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
    final Object outputStreamSyncObj = new Object();
    final Object ticketIssuanceSyncObj = new Object();
    private boolean shouldDie = false;

    public void die() {
        shouldDie = true;
    }

    private synchronized void addCamera(Message msg) {
        if (msg.getType() != Message.MessageType.I_AM_CAMERA) return;
        cameraContext = msg;
        synchronized (dispatcherSemaphores) {
            dispatcherSemaphores.computeIfAbsent(msg.getRoad(), k -> new Semaphore(1));
        }
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

    private synchronized void dispatchTickets(List<Message> ticketsToIssue) {
        synchronized (dispatcherSemaphores) {
            for (var ticket : ticketsToIssue) {
                long road = ticket.getRoad();
                var roadSemaphore = dispatcherSemaphores.get(road);
                System.out.println("Server is dispatching ticket for " + ticket.getPlate());
                try {
                    roadSemaphore.acquire();
                    ticketsToDispatch.computeIfAbsent(road, k -> new ArrayList<>());
                    ticketsToDispatch.get(road).add(ticket);
                } catch (InterruptedException e) {
                    break;
                } finally {
                    roadSemaphore.release();
                }
            }
        }
    }

    private synchronized void addCarObservation(Message msg) {
        if (msg.getType() != Message.MessageType.PLATE || cameraContext == null) return;
        carObservatory.addCarSighting(msg.getPlate(), msg.getTimestamp(),
                cameraContext.getRoad(), cameraContext.getMile(), cameraContext.getLimit());
    }

    public ServerThread(Socket socket) {
        this.clientSocket = socket;
        this.setName("ServerThread " + Math.random());
    }

    private void writeToClient(OutputStream outputStream, Message msg) throws IOException {
        synchronized (outputStreamSyncObj) {
            MessageIO.writeMessage(outputStream, msg);
        }
    }

    public void run() {
        InputStream inputStream;
        OutputStream outputStream;

        try {
            clientSocket.setSoTimeout(3000);
            inputStream = clientSocket.getInputStream();
            outputStream = clientSocket.getOutputStream();
        } catch (IOException e) {
            System.out.println("Can't open client s: " + e);
            return;
        }

        runTicketingThread();

        while (true) {
            if (shouldDie) {
                break;
            }
            Message msg;
            try {
                msg = MessageIO.readMessage(inputStream);
            } catch (SocketTimeoutException ste) {
                continue;
            } catch (IOException e) {
                System.out.println(Thread.currentThread().getName() + ", mode " + threadMode +
                        " -> Error reading from the socket, giving up: " + e);
                break;
            }
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
                    System.out.println("Server got WANT_HEARTBEAT msg");
                    if (msg.getInterval() > 0) {
                        wantHeartbeat = true;
                        runHeartbeatThread(msg, outputStream);
                    }
                }
                case Message.MessageType.I_AM_CAMERA -> {
                    System.out.println("Server got I_AM_CAMERA msg, camera context: road:" + msg.getRoad() +
                            ", mile:" + msg.getMile() + ", limit:" + msg.getLimit());
                    threadMode = ThreadMode.CAMERA;
                    addCamera(msg);
                }
                case Message.MessageType.I_AM_DISPATCHER -> {
                    System.out.println("Server got I_AM_DISPATCHER msg");
                    threadMode = ThreadMode.DISPATCHER;
                    addDispatcher(msg);
                    runDispatcherThreads(outputStream);
                }
                case Message.MessageType.PLATE -> {
                    System.out.println("Server got PLATE msg:" + msg.getPlate() + ", timestamp: " + msg.getTimestamp()
                    +", camera context: road:" + cameraContext.getRoad() + " mile:" + cameraContext.getMile());
                    synchronized (ticketIssuanceSyncObj) {
                        addCarObservation(msg);
                    }
                }
            }
        }

        try {
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing the socket: "+ e);
        }
    }

    private void runDispatcherThreads(OutputStream outputStream) {
        for (var road : dispatcherContext.getDispatcherRoads()) {
            synchronized (dispatcherSemaphores) {
            var roadSemaphore = dispatcherSemaphores.get(road);
            Thread.ofVirtual().start( () -> {
                while (true) {
                    try {
                        synchronized (ticketIssuanceSyncObj) {
                            roadSemaphore.acquire();
                            if (ticketsToDispatch.containsKey(road)) {
                                for (Message ticket : ticketsToDispatch.get(road)) {
                                    try {
                                        writeToClient(outputStream, ticket);
                                        System.out.println("Sent the dispatcher a ticket for " + ticket.getPlate());
                                    } catch (IOException e) {
                                        System.out.println("Unable to write the ticket for " + ticket.getPlate());
                                        return; // TODO see about this
                                    }
                                }
                                ticketsToDispatch.get(road).clear();
                            }
                        }
                    } catch(InterruptedException e){
                        return;
                    } finally{
                        roadSemaphore.release();
                    }
                }
            });
            }
        }
    }

    private void runHeartbeatThread(Message msg, OutputStream outputStream) {
        var heartbeatInterval = msg.getInterval();
        Thread.ofVirtual().start( () -> {
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
    }

    private void runTicketingThread() {
        int ticketCheckInterval = 200; // check for tickets to issue every 200msec

        Thread.ofVirtual().start( () -> {
            while (true) {
                try {
                    Thread.sleep(ticketCheckInterval);
                } catch (InterruptedException e) {
                    break;
                }
                synchronized (ticketIssuanceSyncObj) {
                    dispatchTickets(carObservatory.issueTickets());
                }
            }
        });
    }

}
