package com.protohackers.speed;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class MessageIO {

    private static void writeU8(Writer writer, long u8) throws IOException {
        writer.write((int) u8 & 0xFF);
    }

    private static void writeU16(Writer writer, long u16) throws IOException {
        writer.write((int) u16 & 0xFF00);
        writer.write((int) u16 & 0xFF);
    }


    private static void writeU32(Writer writer, long u32) throws IOException {
        writer.write((int) u32 & 0xFF000000);
        writer.write((int) u32 & 0x00FF0000);
        writer.write((int) u32 & 0x0000FF00);
        writer.write((int) u32 & 0x000000FF);
    }

    private static long readU32(Reader reader) throws IOException {
        long ret = 0;
        ret = ret | ((reader.read() & 0xFF) << 24);
        ret = ret | ((reader.read() & 0xFF) << 16);
        ret = ret | ((reader.read() & 0xFF) << 8);
        ret = ret | (reader.read() & 0xFF);
        return ret;
    }

    private static long readU16(Reader reader) throws IOException {
        long ret = 0;
        ret = ret | ((reader.read() & 0xFF) << 8);
        ret = ret | (reader.read() & 0xFF);
        return ret;
    }

    private static long readU8(Reader reader) throws IOException {
        return reader.read() & 0xFF;
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
            case Message.MessageType.TICKET -> {
                writeString(writer, message.getPlate());
                writeU16(writer, message.getRoad());
                writeU16(writer, message.getMile1());
                writeU32(writer, message.getTimestamp1());
                writeU16(writer, message.getMile2());
                writeU32(writer, message.getTimestamp2());
                writeU16(writer, message.getSpeed());
            }
        }
    }

    public static Message createTicketMessage(String plate,
                                               long road,
                                               long mile1,
                                               long timestamp1,
                                               long mile2,
                                               long timestamp2,
                                               long speed) {
        Message ret = new Message();
        ret.setType(Message.MessageType.TICKET);
        ret.setPlate(plate);
        ret.setRoad(road);
        ret.setMile1(mile1);
        ret.setTimestamp1(timestamp1);
        ret.setMile2(mile2);
        ret.setTimestamp2(timestamp2);
        ret.setSpeed(speed);
        return ret;
    }
    private static Message readTicketMessage(Reader reader) {
        Message ret = new Message();
        ret.setType(Message.MessageType.TICKET);
        try {
            ret.setPlate(readString(reader));
            ret.setRoad(readU16(reader));
            ret.setMile1(readU16(reader));
            ret.setTimestamp1(readU32(reader));
            ret.setMile2(readU16(reader));
            ret.setTimestamp2(readU32(reader));
            ret.setSpeed(readU16(reader));
        } catch (IOException e) {
            return null;
        }
        return ret;

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
                 return readTicketMessage(reader);
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
