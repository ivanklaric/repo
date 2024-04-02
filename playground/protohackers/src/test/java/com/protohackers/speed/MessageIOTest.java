package com.protohackers.speed;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class MessageIOTest {

    private ByteArrayOutputStream buildOutputStreamWithMessage(Message msg) {
        var outputStream = new ByteArrayOutputStream();
        var writer = new OutputStreamWriter(outputStream);
        try {
            MessageIO.writeMessage(writer, msg);
            writer.flush();
        } catch (IOException e) {
            fail(e);
        }
        return outputStream;
    }

    @Test
    public void testErrorMessage() {
        var outputStream = buildOutputStreamWithMessage(MessageIO.createErrorMessage("foo"));
        var reader = new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray()));

        var msg = MessageIO.readMessage(reader);
        assertNotNull(msg);
        assertEquals(msg.getType(), Message.MessageType.ERROR);
        assertEquals(msg.getErrorMessage(), "foo");
    }

    @Test
    public void testPlate() {
        var outputStream = buildOutputStreamWithMessage(MessageIO.createPlateMessage("plate-foo", 123));
        var reader = new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray()));

        var msg = MessageIO.readMessage(reader);
        assertNotNull(msg);
        assertEquals(msg.getType(), Message.MessageType.PLATE);
        assertEquals(msg.getPlate(), "plate-foo");
        assertEquals(msg.getTimestamp(), 123);
    }

    @Test
    public void testTicket() {
        var outputStream = buildOutputStreamWithMessage(
                MessageIO.createTicketMessage("foo-123", 1, 2, 3, 100, 10, 30));
        var reader = new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray()));

        var msg = MessageIO.readMessage(reader);
        assertNotNull(msg);
        assertEquals(Message.MessageType.TICKET, msg.getType());
        assertEquals("foo-123", msg.getPlate());
        assertEquals(1, msg.getRoad());
        assertEquals(2, msg.getMile1());
        assertEquals(3, msg.getTimestamp1());
        assertEquals(100, msg.getMile2());
        assertEquals(10, msg.getTimestamp2());
        assertEquals(30, msg.getSpeed());
    }
}