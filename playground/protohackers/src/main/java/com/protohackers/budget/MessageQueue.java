package com.protohackers.budget;

import java.util.ArrayList;
import java.util.List;

public class MessageQueue {
    private final List<String> messages = new ArrayList<>();

    public synchronized String getMessageAtIndex(int index) {
        if (index >= messages.size())
            return null;
        if (index < 0)
            return null;

        return messages.get(index);
    }

    public synchronized int getUnreadMessages(int lastReadIndex) {
        return messages.size()-lastReadIndex;
    }

    public int getStartingIndex() {
        return 0;
    }

    public synchronized void addMessage(String msg) {
        messages.add(msg);
    }
}
