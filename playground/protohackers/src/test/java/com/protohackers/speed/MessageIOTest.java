package com.protohackers.speed;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class MessageIOTest {

    private ByteArrayOutputStream buildOutputStreamWithMessage(Message msg) {
        var outputStream = new ByteArrayOutputStream();
        try {
            MessageIO.writeMessage(outputStream, msg);
            outputStream.flush();
        } catch (IOException e) {
            fail(e);
        }
        return outputStream;
    }

    @Test
    public void testErrorMessage() {
        var outputStream = buildOutputStreamWithMessage(MessageIO.createErrorMessage("foo"));
        var inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        var msg = MessageIO.readMessage(inputStream);
        assertNotNull(msg);
        assertEquals(msg.getType(), Message.MessageType.ERROR);
        assertEquals(msg.getErrorMessage(), "foo");
    }

    @Test
    public void testPlate() {
        var outputStream = buildOutputStreamWithMessage(MessageIO.createPlateMessage("plate-foo", 123));
        var inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        var msg = MessageIO.readMessage(inputStream);
        assertNotNull(msg);
        assertEquals(msg.getType(), Message.MessageType.PLATE);
        assertEquals(msg.getPlate(), "plate-foo");
        assertEquals(msg.getTimestamp(), 123);
    }

    @Test
    public void testTicket() {
        var outputStream = buildOutputStreamWithMessage(
                MessageIO.createTicketMessage("UN1X", 66, 100, 123456, 110, 123816, 10000));
        var inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        var msg = MessageIO.readMessage(inputStream);
        assertNotNull(msg);
        assertEquals(Message.MessageType.TICKET, msg.getType());
        assertEquals("UN1X", msg.getPlate());
        assertEquals(66, msg.getRoad());
        assertEquals(100, msg.getMile1());
        assertEquals(123456, msg.getTimestamp1());
        assertEquals(110, msg.getMile2());
        assertEquals(123816, msg.getTimestamp2());
        assertEquals(10000, msg.getSpeed());
    }
}