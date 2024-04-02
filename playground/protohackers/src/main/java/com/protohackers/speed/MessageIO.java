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

    private static long readU32(Reader reader) throws IOException {
        return (long) reader.read();
    }

    private static void writeString(Writer writer, String str) throws IOException {
        writer.write(str.length());
        if (!str.isEmpty()) {
            writer.write(str);
        }
    }

    private static String readString(Reader reader) throws IOException {
        int stringSize = reader.read();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < stringSize; i++) {
            builder.append((char)reader.read());
        }
        return builder.toString();
    }

    public static Message createErrorMessage(String message) {
        var ret = new Message();
        ret.setType(Message.MessageType.ERROR);
        ret.setErrorMessage(message);
        return ret;
    }

    private static Message readErrorMessage(Reader reader) {
        Message ret = new Message();
        ret.setType(Message.MessageType.ERROR);
        try {
            ret.setErrorMessage(readString(reader));
        } catch (IOException e) {
            return null;
        }
        return ret;
    }


    public static Message createPlateMessage(String plate, long timestamp) {
        var ret = new Message();
        ret.setType(Message.MessageType.PLATE);
        ret.setPlate(plate);
        ret.setTimestamp(timestamp);
        return ret;
    }

    private static Message readPlateMessage(Reader reader) {
        Message ret = new Message();
        ret.setType(Message.MessageType.PLATE);
        try {
            ret.setPlate(readString(reader));
            ret.setTimestamp(readU32(reader));
        } catch (IOException e) {
            return null;
        }
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

        switch (msgType) {
            case 0x10:
                return readErrorMessage(reader);
            case 0x20:
                return readPlateMessage(reader);
            case 0x21:
//                 ret.setType(Message.MessageType.TICKET);
            case 0x40:
//                ret.setType(Message.MessageType.WANT_HEARTBEAT);
            case 0x41:
//                ret.setType(Message.MessageType.HEARTBEAT);
            case 0x80:
//                ret.setType(Message.MessageType.I_AM_CAMERA);
            case 0x81:
//                ret.setType(Message.MessageType.I_AM_DISPATCHER);
        }
        return null;
    }
}
