package com.protohackers.unusual;

public class UnusualMessage {
    public enum MessageType {
        INSERT, RETRIEVE
    }

    public MessageType getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    private final MessageType type;
    private final String key;
    private String value;

    public UnusualMessage(String rawMessage) {
        if (rawMessage.contains("=")) {
            type = MessageType.INSERT;
            key = rawMessage.substring(0, rawMessage.indexOf("="));
            value = rawMessage.substring(rawMessage.indexOf("=")+1);
        } else {
            type = MessageType.RETRIEVE;
            key = rawMessage;
        }
    }

    public String toString() {
        if (getType() == MessageType.RETRIEVE)
            return getKey();
        return getKey() + "=" + getValue();
    }
}
