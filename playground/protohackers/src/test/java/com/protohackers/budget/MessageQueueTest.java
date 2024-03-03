package com.protohackers.budget;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageQueueTest {

    @Test
    public void testHappyPath() {
        MessageQueue q = new MessageQueue();
        assertEquals(0, q.getUnreadMessages("foo"));
        q.addMessage("first");
        q.addMessage("second");
        q.addMessage("third");
        assertEquals(3, q.getUnreadMessages("foo"));
        assertEquals("first", q.getMessageAtIndex(q.getNextMessageIndex("foo")));
        q.incrementMessageIndex("foo");
        assertEquals(2, q.getUnreadMessages("foo"));
        assertEquals("second", q.getMessageAtIndex(q.getNextMessageIndex("foo")));
        q.incrementMessageIndex("foo");
        assertEquals(1, q.getUnreadMessages("foo"));
        assertEquals("third", q.getMessageAtIndex(q.getNextMessageIndex("foo")));
        q.incrementMessageIndex("foo");
        assertNull(q.getMessageAtIndex(q.getNextMessageIndex("foo")));
        assertEquals(0, q.getUnreadMessages("foo"));
    }

    @Test
    public void testConcurrentUsers() {
        var q = new MessageQueue();
        assertEquals(0, q.getUnreadMessages("foo"));
        assertEquals(0, q.getUnreadMessages("bar"));
        q.addMessage("first");
        q.addMessage("second");
        q.incrementMessageIndex("foo"); // simulate foo read the message
        q.incrementMessageIndex("foo"); // simulate foo read the message
        q.incrementMessageIndex("bar"); // simulate foo read the message
        q.incrementMessageIndex("bar"); // simulate foo read the message
        assertEquals(0, q.getUnreadMessages("baz")); // baz just joined, he shouldn't see previous msgs
    }
}