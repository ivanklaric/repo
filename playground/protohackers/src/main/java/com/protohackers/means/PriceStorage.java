package com.protohackers.means;

import java.util.ArrayList;
import java.util.List;

public class PriceStorage {

    private final List<Message> messages = new ArrayList<>();


    public int getMeanPrice(int minTime, int maxTime) {
        if (minTime > maxTime)
            return 0;

        long totalPrice = 0;
        int numWithinRange = 0;
        for (var msg : messages) {
            if (msg.getTimestamp() >= minTime && msg.getTimestamp() <= maxTime) {
                numWithinRange++;
                totalPrice += msg.getPrice();
            }
        }
        if (numWithinRange == 0)
            return 0;
        long solution = totalPrice / numWithinRange;
        return (int) solution;
    }

    public void storePrice(Message message) {
        messages.add(message);
    }
}
