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

}