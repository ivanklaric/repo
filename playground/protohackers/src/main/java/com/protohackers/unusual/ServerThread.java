package com.protohackers.unusual;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerThread extends Thread {

    private DatagramSocket socket;

    public ServerThread(DatagramSocket socket) {
        this.socket = socket;
    }

    private String processMessage(String inputMessage) {
        return "version=Ken's Key-Value Store 1.0\n";
    }

    public void run() {
        byte[] receiveData = new byte[1024];
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                socket.receive(receivePacket);
            } catch (IOException e) {
                System.out.println("Error receiving data: " + e);
                break;
            }
            String receivedMsg = new String(receivePacket.getData(), 0, receivePacket.getLength());
            String responseMsg = processMessage(receivedMsg);

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
