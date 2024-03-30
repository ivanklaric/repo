package com.protohackers.unusual;

import java.util.HashMap;
import java.util.Map;

public class UnusualDatabase {
    private final Map<String, UnusualMessage> data = new HashMap<>();


    public UnusualDatabase() {
        data.put("version", new UnusualMessage("version=Ken's Key-Value Store 1.0"));
    }

    public UnusualMessage processMessage(UnusualMessage msg) {
        if (msg.getType() == UnusualMessage.MessageType.RETRIEVE) {
            return data.get(msg.getKey());
        }
        if (msg.getKey().equals("version")) { // requests to modify the version must be ignored
            return null;
        }
        data.put(msg.getKey(), msg);
        return null;
    }
}
