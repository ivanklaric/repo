package com.protohackers.unusual;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerThread extends Thread {

    private final DatagramSocket socket;
    private final UnusualDatabase db = new UnusualDatabase();

    public ServerThread(DatagramSocket socket) {
        this.socket = socket;
    }

    private String processMessage(String inputMessage) {
        if (inputMessage.equals("version")) {
            return "version=Ken's Key-Value Store 1.0";
        }
        return db.processMessage(new UnusualMessage(inputMessage));
    }

    public void run() {
        byte[] receiveData = new byte[1024];
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            // Receive request
            try {
                socket.receive(receivePacket);
            } catch (IOException e) {
                System.out.println("Error receiving data: " + e);
                break;
            }
            String receivedMsg = new String(receivePacket.getData(), 0, receivePacket.getLength());
            String responseMsg = processMessage(receivedMsg);
            System.out.println("Received message: [" + receivedMsg + "] Response=[" + responseMsg + "]");
            if (responseMsg == null) {
                continue;
            }

            // Send response
            InetAddress clientAddress = receivePacket.getAddress();
            int clientPort = receivePacket.getPort();
            byte[] responseData = responseMsg.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientAddress, clientPort);

            try {
                socket.send(responsePacket);
            } catch (IOException e) {
                System.out.println("Error sending data: " + e);
                break;
            }
        }
        socket.close();
    }
}
