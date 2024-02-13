package com.protohackers.prime;

import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread {
    private final Socket socket;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            return;
        }
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            return;
        }
        try {
            String jsonString;
            while ((jsonString = reader.readLine()) != null) {
                boolean isValidRequest = RequestResponseObjectManipulation.isValidRequest(jsonString);
                var numberInRequest = RequestResponseObjectManipulation.getNumberFromRequest(jsonString);
                if (!isValidRequest || numberInRequest == null) {
                    writer.write(RequestResponseObjectManipulation.createMalformedResponse());
                } else {
                    writer.write(RequestResponseObjectManipulation.createResponse(PrimeNumberDetector.isPrimeNumber(numberInRequest)));
                }
            }
            writer.close();
            socket.close();
        } catch (IOException e) {
            // probably not worth doing anything in this case
        }
    }
}