package com.protohackers.unusual;

import java.util.HashMap;
import java.util.Map;

public class UnusualDatabase {
    private final Map<String, String> data = new HashMap<>();

    public String processMessage(UnusualMessage msg) {
        if (msg.getType() == UnusualMessage.MessageType.RETRIEVE) {
            return data.get(msg.getKey());
        }
        data.put(msg.getKey(), msg.getValue());
        return null;
    }
}
