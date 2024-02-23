package com.protohackers.means;

import java.util.LinkedList;
import java.util.List;

public class PriceStorage {

    private final List<Message> messages = new LinkedList<>();


    public int getMeanPrice(int minTime, int maxTime) {
        if (minTime > maxTime)
            return 0;

        int totalPrice = 0;
        int numWithinRange = 0;
        for (var msg : messages) {
            if (msg.getTimestamp() >= minTime && msg.getTimestamp() <= maxTime) {
                numWithinRange++;
                totalPrice += msg.getPrice();
            }
        }
        if (numWithinRange == 0)
            return 0;

        return totalPrice / numWithinRange;
    }

    public void storePrice(Message message) {
        int insertAtIndex = 0;
        while (insertAtIndex < messages.size() && messages.get(insertAtIndex).getTimestamp() < message.getTimestamp()) {
            insertAtIndex++;
        }
        messages.add(insertAtIndex, message);
    }
}
