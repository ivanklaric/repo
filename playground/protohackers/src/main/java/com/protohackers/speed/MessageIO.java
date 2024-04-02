package com.protohackers.speed;

import java.io.*;

public class MessageIO {

    private static long getNextUnsignedInt(InputStream inputStream) throws IOException {
        int read = inputStream.read();
        if (read < 0) {
            throw new IOException("Couldn't read from reader.");
        }
        return Byte.toUnsignedLong((byte) (read & 0xff));
    }

    private static void writeU8(OutputStream outputStream, long u8) throws IOException {
        outputStream.write( Byte.toUnsignedInt( (byte)(u8 & 0xFF) ) );
    }

    private static void writeU16(OutputStream outputStream, long u16) throws IOException {
        outputStream.write( Byte.toUnsignedInt((byte)((u16 & 0xFF00) >> 8)) );
        outputStream.write( Byte.toUnsignedInt((byte)(u16 & 0xFF)) );
    }


    private static void writeU32(OutputStream outputStream, long u32) throws IOException {
        outputStream.write(((int)u32 & 0xFF000000) >> 24);
        outputStream.write(((int)u32 & 0xFF0000) >> 16);
        outputStream.write(((int)u32 & 0xFF00) >> 8);
        outputStream.write( (int)u32 & 0xFF);
    }

    private static long readU32(InputStream inputStream) throws IOException {
        long ret = 0;
        ret |= getNextUnsignedInt(inputStream) << 24;
        ret |= getNextUnsignedInt(inputStream) << 16;
        ret |= getNextUnsignedInt(inputStream) << 8;
        ret |= getNextUnsignedInt(inputStream);
        return ret;
    }

    private static long readU16(InputStream inputStream) throws IOException {
        long ret = 0;
        ret |= (getNextUnsignedInt(inputStream) & 0xFF) << 8;
        ret |= getNextUnsignedInt(inputStream) & 0xFF;
        return ret;
    }

    private static long readU8(InputStream inputStream) throws IOException {
        return getNextUnsignedInt(inputStream);
    }


    private static void writeString(OutputStream outputStream, String str) throws IOException {
        outputStream.write(str.length());
        if (!str.isEmpty()) {
            for (char c : str.toCharArray()) {
                outputStream.write(c);
            }
        }
    }

    private static String readString(InputStream inputStream) throws IOException {
        long stringSize = getNextUnsignedInt(inputStream);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < stringSize; i++) {
            builder.append((char)getNextUnsignedInt(inputStream));
        }
        return builder.toString();
    }

    public static Message createErrorMessage(String message) {
        var ret = new Message();
        ret.setType(Message.MessageType.ERROR);
        ret.setErrorMessage(message);
        return ret;
    }

    private static Message readErrorMessage(InputStream inputStream) {
        Message ret = new Message();
        ret.setType(Message.MessageType.ERROR);
        try {
            ret.setErrorMessage(readString(inputStream));
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

    private static Message readPlateMessage(InputStream inputStream) {
        Message ret = new Message();
        ret.setType(Message.MessageType.PLATE);
        try {
            ret.setPlate(readString(inputStream));
            ret.setTimestamp(readU32(inputStream));
        } catch (IOException e) {
            return null;
        }
        return ret;
    }

    private static String formatByteArray(byte[] array) {
        var builder = new StringBuilder();
        for (byte b : array) {
            builder.append(String.format("%02X ", b));
        }
        return builder.toString();
    }

    public static void writeMessage(OutputStream outputStream, Message message) throws IOException {
        writeU8(outputStream, message.getType().errorCode);
        switch(message.getType()) {
            case Message.MessageType.ERROR ->
                writeString(outputStream, message.getErrorMessage());
            case Message.MessageType.PLATE -> {
                writeString(outputStream, message.getPlate());
                writeU32(outputStream, message.getTimestamp());
            }
            case Message.MessageType.TICKET -> {
                writeString(outputStream, message.getPlate());
                writeU16(outputStream, message.getRoad());
                writeU16(outputStream, message.getMile1());
                writeU32(outputStream, message.getTimestamp1());
                writeU16(outputStream, message.getMile2());
                writeU32(outputStream, message.getTimestamp2());
                writeU16(outputStream, message.getSpeed());
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
    private static Message readTicketMessage(InputStream inputStream) {
        Message ret = new Message();
        ret.setType(Message.MessageType.TICKET);
        try {
            ret.setPlate(readString(inputStream));
            ret.setRoad(readU16(inputStream));
            ret.setMile1(readU16(inputStream));
            ret.setTimestamp1(readU32(inputStream));
            ret.setMile2(readU16(inputStream));
            ret.setTimestamp2(readU32(inputStream));
            ret.setSpeed(readU16(inputStream));
        } catch (IOException e) {
            return null;
        }
        return ret;

    }

    public static Message readMessage(InputStream inputStream) {
        long msgType;
        try {
            msgType = readU8(inputStream);
        } catch (IOException e) {
            System.out.println("Error when reading type: " + e);
            return null;
        }
        if (msgType == -1)
            return null;

        switch ((int)msgType) {
            case 0x10:
                return readErrorMessage(inputStream);
            case 0x20:
                return readPlateMessage(inputStream);
            case 0x21:
                 return readTicketMessage(inputStream);
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
