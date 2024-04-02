package com.protohackers.speed;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class MessageIOTest {

    @Test
    public void testErrorMessage() {
        var outputStream = new ByteArrayOutputStream();
        var writer = new OutputStreamWriter(outputStream);
        try {
            MessageIO.writeMessage(writer, MessageIO.createErrorMessage("foo"));
        } catch (IOException e) {
            fail(e);
        }
        try {
            writer.flush();
        } catch (IOException e) {
            fail(e);
        }
        byte[] buffer = outputStream.toByteArray();
        var inputStream = new ByteArrayInputStream(buffer);
        var reader = new InputStreamReader(inputStream);

        var msg = MessageIO.readMessage(reader);
        assertNotNull(msg);
        assertEquals(msg.getType(), Message.MessageType.ERROR);
        assertEquals(msg.getErrorMessage(), "foo");
    }

}