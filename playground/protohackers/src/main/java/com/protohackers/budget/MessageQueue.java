package com.protohackers.budget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageQueue {
    private final List<String> messages = new ArrayList<>();
    private final Map<String, Integer> userIndexMap = new HashMap<>();

    public synchronized String getMessageAtIndex(int index) {
        if (index >= messages.size())
            return null;
        if (index < 0)
            return null;

        return messages.get(index);
    }

    public synchronized int getUnreadMessages(String user) {
        return messages.size()-getNextMessageIndex(user);
    }

    public synchronized int getNextMessageIndex(String user) {
        if (!userIndexMap.containsKey(user)) {
            userIndexMap.put(user, getHighWaterMark());
        }
        return userIndexMap.get(user);
    }

    public synchronized void incrementMessageIndex(String user) {
        if (!userIndexMap.containsKey(user)) {
            userIndexMap.put(user, getHighWaterMark());
        }
        userIndexMap.put(user, userIndexMap.get(user)+1);
    }

    private synchronized int getHighWaterMark() {
        int maxIndex = 0;
        for (var user : userIndexMap.keySet()) {
            if (userIndexMap.get(user) > maxIndex) {
                maxIndex = userIndexMap.get(user);
            }
        }
        return maxIndex;
    }

    public synchronized void addMessage(String msg) {
        messages.add(msg);
    }
}
