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
    boolean timeoutFail = false;

    @Test
    public void TestHeartbeat() {
        Object syncObj = new Object();

        Thread serverThread = runServerThread();
        Thread clientThread = runClientThread("heartbeatTest", syncObj,
                List.of( MessageIO.createWantHeartBeatMessage(10)),
                MessageIO.createHeartBeatMessage());
        Thread timeoutThread = runTimeoutThread("heartbeatTest", syncObj);

        joinThreadsAndCheckResponse(timeoutThread, List.of(clientThread), serverThread);
    }

    @Test
    public void TestInvalidMessage() {
        Object syncObj = new Object();

        Thread serverThread = runServerThread();
        Thread clientThread = runClientThread("invalidMsgTest", syncObj,
                List.of( MessageIO.createInvalidMessage()),
                MessageIO.createErrorMessage("Invalid message sent"));
        Thread timeoutThread = runTimeoutThread("invalidMsgTest", syncObj);

        joinThreadsAndCheckResponse(timeoutThread, List.of(clientThread), serverThread);
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

        joinThreadsAndCheckResponse(timeoutThread, List.of(clientThread), serverThread);
    }

    @Test
    public void TestSimpleProtocol() {
        Object syncObj = new Object();

        System.out.println("Main thread name is " + Thread.currentThread().getName());
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
        Thread dispatcherThread = runClientThread("dispatcher", syncObj,
                List.of(
                        MessageIO.createIAmDispatcherMessage(new long[] {123})
                ),
                MessageIO.createTicketMessage("UN1X", 123, 8, 0, 9, 45, 8000));
        Thread timeoutThread = runTimeoutThread("dispatcher", syncObj);
        joinThreadsAndCheckResponse(timeoutThread,
                List.of(camera1Thread, camera2Thread, dispatcherThread),
                serverThread);
    }

    private void joinThreadsAndCheckResponse(Thread timeoutThread, List<Thread> threadsToJoin, Thread serverThread) {
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
            if (msgToExpect != null) {
                Message msg = null;
                try {
                    msg = client.retrieveMessage();
                } catch (IOException e) {
                    fail("Failed retrieving " + msgToExpect.getType() + " message: " + e);
                }
                assertNotNull(msg);
                assertEquals(msgToExpect.getType(), msg.getType());
                System.out.println(Thread.currentThread().getName() + " -> Message " + msg.getType() + " received.");
            }
            synchronized (syncObj) {
                responseReceived.put(Thread.currentThread().getName(), true);
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
