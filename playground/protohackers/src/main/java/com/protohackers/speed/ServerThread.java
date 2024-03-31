package com.protohackers.speed;

import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread {
    private final Socket clientSocket;
    public ServerThread(Socket socket) {
        this.clientSocket = socket;
    }
    public void run() {
        Reader clientReader;
        try {
            clientReader = new InputStreamReader(clientSocket.getInputStream());
        } catch (IOException e) {
            System.out.println("Can't open client Input stream: " + e);
            return;
        }

        while (true) {
            var msg = MessageIO.readMessage(clientReader);
            if (msg == null) {
                break;
            }
        }
    }
}
