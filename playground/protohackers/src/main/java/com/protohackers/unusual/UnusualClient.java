package com.protohackers.unusual;

import java.io.IOException;
import java.net.*;

public class UnusualClient {
    public static void sendMessage(String server, int port, UnusualMessage msg) {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] data = msg.toString().getBytes();
            InetAddress address = InetAddress.getByName(server);
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            socket.send(packet);
        } catch (SocketException e) {
            System.out.println("Can't open a UDP socket: " + e);
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + e);
        } catch (IOException e) {
            System.out.println("IO Exception: " + e);
        }

    }
}
