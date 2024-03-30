package com.protohackers.unusual;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnusualDatabaseTest {

    @Test
    public void testRandomCases() {
        var db = new UnusualDatabase();
        assertNull(db.processMessage(new UnusualMessage("foo=bar")));
        assertEquals("bar", db.processMessage(new UnusualMessage("foo")));
        assertNull(db.processMessage(new UnusualMessage("foo=baz")));
        assertEquals("baz", db.processMessage(new UnusualMessage("foo")));
        assertNull(db.processMessage(new UnusualMessage("foo=")));
        assertEquals("", db.processMessage(new UnusualMessage("foo")));
        assertNull(db.processMessage(new UnusualMessage("=foo")));
        assertEquals("foo", db.processMessage(new UnusualMessage("")));
    }

}