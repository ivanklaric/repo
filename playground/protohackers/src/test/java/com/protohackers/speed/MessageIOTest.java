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
        assertEquals(Message.MessageType.ERROR, msg.getType());
        assertEquals("foo", msg.getErrorMessage());
    }

    @Test
    public void testPlate() {
        var outputStream = buildOutputStreamWithMessage(MessageIO.createPlateMessage("plate-foo", 123));
        var inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        var msg = MessageIO.readMessage(inputStream);
        assertNotNull(msg);
        assertEquals(Message.MessageType.PLATE, msg.getType());
        assertEquals("plate-foo", msg.getPlate());
        assertEquals(123, msg.getTimestamp());
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

    @Test
    public void testWantHeartbeat() {
        var outputStream = buildOutputStreamWithMessage( MessageIO.createWantHeartBeatMessage(100) );
        var inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        var msg = MessageIO.readMessage(inputStream);
        assertNotNull(msg);
        assertEquals(Message.MessageType.WANT_HEARTBEAT, msg.getType());
        assertEquals(100, msg.getInterval());
    }

    @Test
    public void testHeartbeat() {
        var outputStream = buildOutputStreamWithMessage( MessageIO.createHeartBeatMessage() );
        var inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        var msg = MessageIO.readMessage(inputStream);
        assertNotNull(msg);
        assertEquals(Message.MessageType.HEARTBEAT, msg.getType());
    }

    @Test
    public void testIAmCamera() {
        var outputStream = buildOutputStreamWithMessage(
                MessageIO.createIAmCameraMessage(100, 200, 250));
        var inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        var msg = MessageIO.readMessage(inputStream);
        assertNotNull(msg);
        assertEquals(Message.MessageType.I_AM_CAMERA, msg.getType());
        assertEquals(100, msg.getRoad());
        assertEquals(200, msg.getMile());
        assertEquals(250, msg.getLimit());
    }

    @Test
    public void testIAmDispatcher() {
        var outputStream = buildOutputStreamWithMessage(
                MessageIO.createIAmDispatcherMessage(new long[] {101, 66, 200, 300, 10000, 25000, 30000}));
        var inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        var msg = MessageIO.readMessage(inputStream);
        assertNotNull(msg);
        assertEquals(Message.MessageType.I_AM_DISPATCHER, msg.getType());
        assertNotNull(msg.getDispatcherRoads());
        assertEquals(7, msg.getDispatcherRoads().size());
        assertEquals(101, msg.getDispatcherRoads().get(0));
        assertEquals(66, msg.getDispatcherRoads().get(1));
        assertEquals(200, msg.getDispatcherRoads().get(2));
        assertEquals(300, msg.getDispatcherRoads().get(3));
        assertEquals(30000, msg.getDispatcherRoads().get(6));
    }

}