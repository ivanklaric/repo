package com.protohackers.speed;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.*;

public class SingleClientTests {

    boolean responseReceived = false;
    boolean timeoutFail = false;

    @Test
    public void TestHeartbeat() {
        Object syncObj = new Object();

        Thread serverThread = runServerThread();
        Thread clientThread = runClientThread(syncObj,
                MessageIO.createWantHeartBeatMessage(10),
                MessageIO.createHeartBeatMessage());
        Thread timeoutThread = runTimeoutThread(syncObj);

        joinThreadsAndCheckResponse(timeoutThread, clientThread, serverThread);
    }

    @Test
    public void TestInvalidMessage() {
        Object syncObj = new Object();

        Thread serverThread = runServerThread();
        Thread clientThread = runClientThread(syncObj,
                MessageIO.createInvalidMessage(),
                MessageIO.createErrorMessage("Invalid message sent"));
        Thread timeoutThread = runTimeoutThread(syncObj);

        joinThreadsAndCheckResponse(timeoutThread, clientThread, serverThread);
    }

    private void joinThreadsAndCheckResponse(Thread timeoutThread, Thread clientThread, Thread serverThread) {
        try {
            timeoutThread.join();
            clientThread.interrupt();
            clientThread.join();
            serverThread.interrupt();
            serverThread.join();
        } catch (InterruptedException e) {
            fail("Failed waiting for the timeout: " + e);
        }
        if (timeoutFail) {
            fail("Client didn't receive heartbeat in required time");
        }

    }

    private Thread runTimeoutThread(Object syncObj) {
        Thread timeoutThread = new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            synchronized ((syncObj)) {
                if (!responseReceived) {
                    timeoutFail = true;
                }
            }
        });
        timeoutThread.start();
        return timeoutThread;
    }

    private Thread runClientThread(Object syncObj, Message msgToSend, Message msgToExpect) {
        Thread clientThread = new Thread(() -> {
            SpeedClient client = new SpeedClient("localhost", 9003);
            try {
                System.out.println("Client is sending the want_heartbeat message");
                client.sendMessage(msgToSend);
            } catch (IOException e) {
                fail("Failed sending WANT_HEARTBEAT_MESSAGE: " +e);
            }
            Message msg = null;
            try {
                msg = client.retrieveMessage();
            } catch (IOException e) {
                fail("Failed retrieving HEARTBEAT message: " + e);
            }
            System.out.println("Heartbeat message received.");
            assertNotNull(msg);
            assertEquals(msgToExpect.getType(), msg.getType());
            synchronized (syncObj) {
                responseReceived = true;
            }
            client.close();
        });
        clientThread.start();
        return clientThread;
    }

    private static Thread runServerThread() {
        Thread serverThread = new Thread(() -> {
            ServerThread workerThread = null;
            try (ServerSocket serverSocket = new ServerSocket(9003)) {
                try {
                    serverSocket.setSoTimeout(3000);
                    var socket = serverSocket.accept();
                    workerThread = new ServerThread(socket);
                    workerThread.start();
                } catch (SocketTimeoutException ignored) {
                } catch (IOException e) {
                    fail("Error creating a socket: " + e);
                    return;
                } finally {
                    serverSocket.close();
                }
                while (!Thread.interrupted()) {
                    // do nothing
                }
                if (workerThread != null) {
                    workerThread.die();
                    workerThread.interrupt();
                }
            } catch (IOException e) {
                fail("Error when opening server: " + e);
            }
        });
        serverThread.start();
        return serverThread;
    }
}
