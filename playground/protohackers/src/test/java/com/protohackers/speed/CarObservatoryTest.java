package com.protohackers.speed;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CarObservatoryTest {

    @Test
    public void simpleScenario() {
        var observatory = new CarObservatory();
        observatory.addCarSighting("UN1X", 0, 123, 8, 60);
        observatory.addCarSighting("UN1X", 45, 123, 9, 60);
        var tickets = observatory.issueTickets();
        assertNotNull(tickets);
        assertEquals(1, tickets.size());
        var ticket = tickets.getFirst();
        assertEquals("UN1X", ticket.getPlate());
        assertEquals(123, ticket.getRoad());
        assertEquals(8, ticket.getMile1());
        assertEquals(9, ticket.getMile2());
        assertEquals(0, ticket.getTimestamp1());
        assertEquals(45, ticket.getTimestamp2());
        assertEquals(8000, ticket.getSpeed());
    }

    @Test
    public void onlyOneTicketPerDayScenario() {
        var observatory = new CarObservatory();
        observatory.addCarSighting("UN1X", 0, 123, 8, 60);
        observatory.addCarSighting("UN1X", 45, 123, 9, 60);
        observatory.addCarSighting("UN1X", 90, 123, 10, 60);
        var tickets = observatory.issueTickets();
        assertNotNull(tickets);
        assertEquals(1, tickets.size());
        var ticket = tickets.getFirst();
        assertEquals("UN1X", ticket.getPlate());
        assertEquals(123, ticket.getRoad());
        assertEquals(8, ticket.getMile1());
        assertEquals(9, ticket.getMile2());
        assertEquals(0, ticket.getTimestamp1());
        assertEquals(45, ticket.getTimestamp2());
        assertEquals(8000, ticket.getSpeed());
    }

}