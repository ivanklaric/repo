package com.protohackers.means;

import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread {
    private final Socket socket;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    private DataInputStream openInputStream() {
        DataInputStream input;
        try {
            input = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("Error opening input stream: " + e);
            return null;
        }
        return input;
    }

    private DataOutputStream openOutputStream() {
        DataOutputStream output;
        try {
            output = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Error opening the output stream: " + e);
            return null;
        }
        return output;
    }

    private byte[] getNextMessage(DataInputStream inputStream) {
        byte[] buffer = new byte[9];

        for (int i = 0; i < 9; i++) {
            try {
                buffer[i] = inputStream.readByte();
            } catch (EOFException e) {
                return null; // end of file reached, return null
            } catch (IOException e) {
                return null; // something generic was wrong with the string, still return null
            }
        }
        return buffer;
    }

    public void run() {
        var inputStream = openInputStream();
        var outputStream = openOutputStream();
        var priceStorage = new PriceStorage();

        if (inputStream == null || outputStream == null) return;

        byte[] rawMessage;
        while ((rawMessage = getNextMessage(inputStream)) != null) {
            var message = new Message(rawMessage);
            if (message.getType() == Message.MessageType.QUERY) {
                int mean = priceStorage.getMeanPrice(message.getMinTime(), message.getMaxTime());
                try {
                    outputStream.writeInt(mean);
                } catch (IOException e) {
                    break; // unable to write, better give up altogether
                }
            }
            if (message.getType() == Message.MessageType.INSERT) {
                priceStorage.storePrice(message);
            }
        }

        try {
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            // ignore, at this the thread will die anyhow
        }

    }
}
