package com.protohackers.unusual;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnusualMessageTest {

    private void testMessage(String rawMsg,
                             UnusualMessage.MessageType expectedType,
                             String expectedKey,
                             String expectedValue) {
        var msg = new UnusualMessage(rawMsg);
        assertEquals(expectedType, msg.getType());
        assertEquals(expectedKey, msg.getKey());
        assertEquals(expectedValue, msg.getValue());
    }

    @Test
    public void testVariousMessages() {
        testMessage("foo=bar", UnusualMessage.MessageType.INSERT, "foo", "bar");
        testMessage("foo=bar=baz", UnusualMessage.MessageType.INSERT, "foo", "bar=baz");
        testMessage("foo=", UnusualMessage.MessageType.INSERT, "foo", "");
        testMessage("foo===", UnusualMessage.MessageType.INSERT, "foo", "==");
        testMessage("=foo", UnusualMessage.MessageType.INSERT, "", "foo");
        testMessage("message", UnusualMessage.MessageType.RETRIEVE, "message", null);
    }

}