package com.protohackers.means;

public class Message {
    private final byte[] rawMessage;

    public Message(byte[] rawMessage) {
        this.rawMessage = rawMessage;
    }

    public enum MessageType { INSERT, QUERY}

    public MessageType getType() {
        if ((char)rawMessage[0] == 'I')
            return MessageType.INSERT;
        return MessageType.QUERY;
    }

    public int firstInt() {
        int ret = 0;
        ret = ret | Byte.toUnsignedInt(rawMessage[4]);
        ret = ret | Byte.toUnsignedInt(rawMessage[3]) << 8;
        ret = ret | Byte.toUnsignedInt(rawMessage[2]) << 16;
        ret = ret | Byte.toUnsignedInt(rawMessage[1]) << 24;
        return ret;
    }

    public int secondInt() {
        int ret = 0;
        ret = ret | Byte.toUnsignedInt(rawMessage[8]);
        ret = ret | Byte.toUnsignedInt(rawMessage[7]) << 8;
        ret = ret | Byte.toUnsignedInt(rawMessage[6]) << 16;
        ret = ret | Byte.toUnsignedInt(rawMessage[5]) << 24;
        return ret;
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
