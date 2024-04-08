package com.protohackers.speed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class SpeedClient {
    private Socket socket;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;


    public SpeedClient(String hostname, int port) {
        InetAddress host;
        socket = null;
        try {
            host = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            return;
        }
        if (host == null) return;
        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            socket = null;
        }
        if (socket == null) return;

        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            inputStream = null;
            outputStream = null;
        }
    }

    public boolean isReady() {
        return inputStream != null && outputStream != null;
    }

    public void sendMessage(Message msg) throws IOException {
        MessageIO.writeMessage(outputStream, msg);
    }

    public Message retrieveMessage() throws IOException {
        System.out.println(Thread.currentThread().getName() + " -> Client is reading a message");
        return MessageIO.readMessage(inputStream);
    }

    public void close() {
        try {
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (IOException ignored) {
        } finally {
            socket = null;
            inputStream = null;
            outputStream = null;
        }
    }
}
