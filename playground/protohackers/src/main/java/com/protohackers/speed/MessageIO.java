package com.protohackers.speed;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class MessageIO {

    private static void writeU8(Writer writer, long u8) throws IOException {
        writer.write((int) u8);
    }

    private static void writeU32(Writer writer, long u32) throws IOException {
        writer.write((int) u32);
    }


    private static void writeString(Writer writer, String str) throws IOException {
        writer.write(str.length());
        if (!str.isEmpty()) {
            writer.write(str);
        }
    }

    public static Message createErrorMessage(String message) {
        var ret = new Message();
        ret.setType(Message.MessageType.ERROR);
        ret.setErrorMessage(message);
        return ret;
    }

    public static void writeMessage(Writer writer, Message message) throws IOException {
        writeU8(writer, message.getType().errorCode);
        switch(message.getType()) {
            case Message.MessageType.ERROR ->
                writeString(writer, message.getErrorMessage());
            case Message.MessageType.PLATE -> {
                writeString(writer, message.getPlate());
                writeU32(writer, message.getTimestamp());
            }
        }
    }

    public static Message readMessage(Reader reader) {
        int msgType;
        try {
            msgType = reader.read();
        } catch (IOException e) {
            System.out.println("Error when reading type: " + e);
            return null;
        }
        if (msgType == -1)
            return null;

        Message ret = new Message();
        switch (msgType) {
            case 0x10:
                ret.setType(Message.MessageType.ERROR);
                break;
            case 0x20:
                ret.setType(Message.MessageType.PLATE);
                break;
            case 0x21:
                ret.setType(Message.MessageType.TICKET);
                break;
            case 0x40:
                ret.setType(Message.MessageType.WANT_HEARTBEAT);
                break;
            case 0x41:
                ret.setType(Message.MessageType.HEARTBEAT);
                break;
            case 0x80:
                ret.setType(Message.MessageType.I_AM_CAMERA);
                break;
            case 0x81:
                ret.setType(Message.MessageType.I_AM_DISPATCHER);
                break;
            default:
                return null; // invalid message
        }
        return ret;
    }
}
