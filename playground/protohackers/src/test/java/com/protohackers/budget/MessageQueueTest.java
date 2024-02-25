package com.protohackers.budget;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageQueueTest {

    @Test
    public void testHappyPath() {
        MessageQueue q = new MessageQueue();
        int lastIndex = q.getStartingIndex();
        assertEquals(0, q.getUnreadMessages(lastIndex));
        q.addMessage("first");
        q.addMessage("second");
        q.addMessage("third");
        assertEquals(3, q.getUnreadMessages(lastIndex));
        assertEquals("first", q.getMessageAtIndex(lastIndex++));
        assertEquals(2, q.getUnreadMessages(lastIndex));
        assertEquals("second", q.getMessageAtIndex(lastIndex++));
        assertEquals(1, q.getUnreadMessages(lastIndex));
        assertEquals("third", q.getMessageAtIndex(lastIndex++));
        assertNull(q.getMessageAtIndex(lastIndex));
        assertEquals(0, q.getUnreadMessages(lastIndex));
    }

}