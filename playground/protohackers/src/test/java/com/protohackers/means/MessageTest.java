package com.protohackers.means;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {


    private void testMessage(byte[] raw, Message.MessageType expectedType, int firstInt, int secondInt) {
        var msg = new Message(raw);
        assertSame(expectedType, msg.getType());
        assertEquals(firstInt, msg.firstInt());
        assertEquals(secondInt, msg.secondInt());
    }
    @Test
    public void testDecoding() {
        testMessage(new byte[] {0x49, 0x00, 0x00, 0x30, 0x39, 0x00, 0x00, 0x00, 0x65},
                Message.MessageType.INSERT, 12345, 101);
        testMessage(new byte[] {0x51, 0x00, 0x00, 0x03, (byte)0xE8, 0x00, 0x01, (byte)0x86, (byte)0xA0},
                Message.MessageType.QUERY, 1000, 100000);

    }

}