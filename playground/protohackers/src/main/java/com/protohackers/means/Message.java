package com.protohackers.means;

public class Message {
    private final byte[] rawMessage;

    public Message(byte[] rawMessage) {
        this.rawMessage = rawMessage;
    }

    public enum MessageType { INSERT, QUERY}

    public MessageType getType() {
        return MessageType.INSERT;
    }

    public int firstInt() {
        return 1;
    }

    public int secondInt() {
        return 2;
    }

    public int getTimestamp() {
        return firstInt();
    }

    public int getPrice() {
        return secondInt();
    }

    public int getMinTime() {
        return firstInt();
    }

    public int getMaxTime() {
        return secondInt();
    }
}
