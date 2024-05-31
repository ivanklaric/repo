package com.protohackers.speed;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class ProtocolTests {

    Map<String, Boolean> responseReceived = new ConcurrentHashMap<>();
    Map<String, List<Message>> messagesReceived = new ConcurrentHashMap<>();
    boolean timeoutFail = false;

    @Test
    public void TestHeartbeat() {
        Object syncObj = new Object();

        Thread serverThread = runServerThread();
        Thread clientThread = runClientThread("heartbeatTest", syncObj,
                List.of( MessageIO.createWantHeartBeatMessage(10)),
                MessageIO.createHeartBeatMessage());
        Thread timeoutThread = runTimeoutThread("heartbeatTest", syncObj);

        joinThreads(timeoutThread, List.of(clientThread), serverThread);
    }

    @Test
    public void TestInvalidMessage() {
        Object syncObj = new Object();

        Thread serverThread = runServerThread();
        Thread clientThread = runClientThread("invalidMsgTest", syncObj,
                List.of( MessageIO.createInvalidMessage()),
                MessageIO.createErrorMessage("Invalid message sent"));
        Thread timeoutThread = runTimeoutThread("invalidMsgTest", syncObj);

        joinThreads(timeoutThread, List.of(clientThread), serverThread);
    }

    @Test
    public void TestCantChangeClientType() {
        Object syncObj = new Object();

        var messages = new ArrayList<Message>();
        messages.add(MessageIO.createIAmCameraMessage(10, 10, 10));
        messages.add(MessageIO.createIAmDispatcherMessage(new long[] {10}));
        Thread serverThread = runServerThread();
        Thread clientThread = runClientThread("client", syncObj, messages,
                MessageIO.createErrorMessage("Invalid message sent"));
        Thread timeoutThread = runTimeoutThread("client", syncObj);

        joinThreads(timeoutThread, List.of(clientThread), serverThread);
    }

    @Test
    public void TestSimpleProtocol() {
        Object syncObj = new Object();

        System.out.println("Main thread name is " + Thread.currentThread().getName());
        Thread serverThread = runServerThread();
        Thread camera1Thread = runClientThread("camera1", syncObj,
                List.of(
                        MessageIO.createIAmCameraMessage(124, 8, 60),
                        MessageIO.createPlateMessage("L1N00X", 0)),
                null);
        Thread camera2Thread = runClientThread("camera2", syncObj,
                List.of(
                        MessageIO.createIAmCameraMessage(124, 9, 60),
                        MessageIO.createPlateMessage("L1N00X", 45)
                ), null);
        Thread dispatcherThread = runClientThread("dispatcher", syncObj,
                List.of(
                        MessageIO.createIAmDispatcherMessage(new long[] {124})
                ),
                MessageIO.createTicketMessage("L1N00X", 124, 8, 0, 9, 45, 8000));
        Thread timeoutThread = runTimeoutThread("dispatcher", syncObj);
        joinThreads(timeoutThread,
                List.of(camera1Thread, camera2Thread, dispatcherThread),
                serverThread);
    }

    @Test
    public void Test100Cars() {
        // First, prep the data
        List<Message> expectedTickets = new ArrayList<>();
        List<Message> camera1Messages = new ArrayList<>();
        camera1Messages.add(MessageIO.createIAmCameraMessage(900, 8, 60));
        List<Message> camera2Messages = new ArrayList<>();
        camera2Messages.add(MessageIO.createIAmCameraMessage(900, 9, 60));
        int road = 900;
        for (int i = 0; i < 200; i++) {
            long startTimestamp = (long) (Math.random()*100000.0);
            long endTimestamp = (long) (Math.random()*100) + startTimestamp+1;
            String carPlate = "car-" + i;
            camera1Messages.add(MessageIO.createPlateMessage(carPlate, startTimestamp));
            camera2Messages.add(MessageIO.createPlateMessage(carPlate, endTimestamp));
            double timeDiff = (double) (endTimestamp - startTimestamp) / 3600;
            long expectedSpeed = Math.round( (double) 1 / timeDiff) * 100;
            if (expectedSpeed > 6000) {
                expectedTickets.add(MessageIO.createTicketMessage(carPlate, road, 8, startTimestamp, 9, endTimestamp, expectedSpeed*100));
                System.out.println("IsSpeeding: plate " + carPlate +", expected speed " + expectedSpeed);
            } else {
                System.out.println("NotSpeeding: plate " + carPlate +", expected speed " + expectedSpeed);
            }
        }

        // then, create the threads
        Object syncObj = new Object();

        Thread serverThread = runServerThread();
        Thread camera1Thread = runClientThread("camera1", syncObj, camera1Messages, null);
        Thread camera2Thread = runClientThread("camera2", syncObj, camera2Messages, null);
        Thread dispatcherThread = runDispatcherThread("dispatchers", syncObj,
                List.of(
                        MessageIO.createIAmDispatcherMessage(new long[] {900})
                ));
        Thread timeoutThread = runTimeoutThread("dispatchers", syncObj);
        joinThreads(timeoutThread,
                List.of(camera1Thread, camera2Thread, dispatcherThread),
                serverThread);
        assertNotNull(messagesReceived.get("dispatchers"));
//        assertArrayEquals(expectedTickets.toArray(), messagesReceived.get("dispatchers").toArray());
        assertEquals(expectedTickets.size(), messagesReceived.get("dispatchers").size());
    }

    @Test
    public void TestMultipleDispatchers() {
        Object syncObj = new Object();

        Thread serverThread = runServerThread();
        Thread camera1Thread = runClientThread("camera1", syncObj,
                List.of(
                        MessageIO.createIAmCameraMessage(123, 8, 60),
                        MessageIO.createPlateMessage("UN1X", 0)),
                null);
        Thread camera2Thread = runClientThread("camera2", syncObj,
                List.of(
                        MessageIO.createIAmCameraMessage(123, 9, 60),
                        MessageIO.createPlateMessage("UN1X", 45)
                ), null);
        Thread dispatcherThread1 = runClientThread("dispatchers", syncObj,
                List.of(
                        MessageIO.createIAmDispatcherMessage(new long[] {123})
                ),
                null, true);
        Thread dispatcherThread2 = runClientThread("dispatchers", syncObj,
                List.of(
                        MessageIO.createIAmDispatcherMessage(new long[] {123, 456})
                ),
                null, true);
        Thread timeoutThread = runTimeoutThread("dispatchers", syncObj);
        joinThreads(timeoutThread,
                List.of(camera1Thread, camera2Thread, dispatcherThread1, dispatcherThread2),
                serverThread);
        assertNotNull(messagesReceived.get("dispatchers"));
        assertEquals(1, messagesReceived.get("dispatchers").size());
        assertEquals(Message.MessageType.TICKET, messagesReceived.get("dispatchers").getFirst().getType());
    }

    private void joinThreads(Thread timeoutThread, List<Thread> threadsToJoin, Thread serverThread) {
        try {
            timeoutThread.join();
            serverThread.interrupt();
            serverThread.join();
            for (Thread t : threadsToJoin) {
                t.interrupt();
                t.join();
            }
        } catch (InterruptedException e) {
            fail("Failed waiting for the timeout: " + e);
        }
        if (timeoutFail) {
            fail("Client didn't receive expected message in required time");
        }
    }

    private Thread runTimeoutThread(String threadName, Object syncObj) {
        Thread timeoutThread = new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            synchronized ((syncObj)) {
                if (!responseReceived.get(threadName)) {
                    timeoutFail = true;
                }
            }
        });
        responseReceived.put(threadName, false);
        timeoutThread.start();
        return timeoutThread;
    }

    private Thread runClientThread(String threadName, Object syncObj, List<Message> msgsToSend, Message msgToExpect) {
        return runClientThread(threadName, syncObj, msgsToSend, msgToExpect, true);
    }

    private Thread runDispatcherThread(String threadName, Object syncObj, List<Message> msgsToSend) {
        Thread clientThread = new Thread(() -> {
            SpeedClient client = new SpeedClient("localhost", 9003);
            if (!client.isReady()) {
                fail("Client couldn't connect");
                return;
            }
            try {
                for (Message msgToSend : msgsToSend) {
                    client.sendMessage(msgToSend);
                    System.out.println(Thread.currentThread().getName() +
                            " -> Client sent the " + msgToSend.getType() + " message");
                }
            } catch (IOException e) {
                fail("Failed sending message: " +e);
            }
            while (!Thread.interrupted()) {
                Message msg = null;
                try {
                    msg = client.retrieveMessage();
                } catch (IOException ignored) {
                }
                synchronized (syncObj) {
                    if (msg != null) {
                        System.out.println(Thread.currentThread().getName() + " -> Message " + msg.getType() + " received for " + msg.getPlate());
                        messagesReceived.computeIfAbsent(Thread.currentThread().getName(), k -> new ArrayList<>());
                        messagesReceived.get(Thread.currentThread().getName()).add(msg);
                        responseReceived.put(Thread.currentThread().getName(), true);
                    }
                }
            }
            client.close();
        }, threadName);
        responseReceived.put(threadName, false);
        clientThread.start();
        return clientThread;
    }

    private Thread runClientThread(String threadName, Object syncObj, List<Message> msgsToSend, Message msgToExpect, boolean expectMessage) {
        Thread clientThread = new Thread(() -> {
            SpeedClient client = new SpeedClient("localhost", 9003);
            if (!client.isReady()) {
                fail("Client couldn't connect");
                return;
            }
            try {
                for (Message msgToSend : msgsToSend) {
                    client.sendMessage(msgToSend);
                    System.out.println(Thread.currentThread().getName() +
                            " -> Client sent the " + msgToSend.getType() + " message");
                }
            } catch (IOException e) {
                fail("Failed sending message: " +e);
            }
            Message msg = null;
            try {
                msg = client.retrieveMessage();
            } catch (IOException ignored) {
            }
            if (expectMessage) {
                if (msgToExpect != null) {
                    assertNotNull(msg);
                    assertEquals(msgToExpect.getType(), msg.getType());
                }
            } else {
                assertNull(msg, "Client " + threadName +" isn't expected to receive a message.");
            }

            synchronized (syncObj) {
                if (msg != null) {
                    System.out.println(Thread.currentThread().getName() + " -> Message " + msg.getType() + " received.");
                    messagesReceived.computeIfAbsent(Thread.currentThread().getName(), k -> new ArrayList<>());
                    messagesReceived.get(Thread.currentThread().getName()).add(msg);
                    responseReceived.put(Thread.currentThread().getName(), true);
                }
            }
            client.close();
        }, threadName);
        responseReceived.put(threadName, false);
        clientThread.start();
        return clientThread;
    }

    private static Thread runServerThread() {
        List<ServerThread> threads = new ArrayList<>();

        Thread serverThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(9003)) {
                while (!Thread.interrupted()) {
                    try {
                        serverSocket.setSoTimeout(500);
                        var socket = serverSocket.accept();
                        var workerThread = new ServerThread(socket);
                        threads.add(workerThread);
                        workerThread.start();
                    } catch (SocketTimeoutException ignored) {
                        continue;
                    } catch (IOException e) {
                        fail("Error creating a socket: " + e);
                        return;
                    }
                }
                serverSocket.close();
                System.out.println(Thread.currentThread().getName() + " interrupted, leaving main loop");
            } catch (IOException e) {
                fail("Error creating a socket: " + e);
            }

            for (var t : threads) {
                t.die();
                t.interrupt();
            }
        }, "main-server-thread");
        serverThread.start();
        return serverThread;
    }
}
