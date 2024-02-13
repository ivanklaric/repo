package com.protohackers.prime;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class RequestResponseObjectManipulationTest {

    @Test
    void isValidRequest() {
        var assertTrueList = new ArrayList<String>();
        var assertFalseList = new ArrayList<String>();


        assertFalseList.add("{'foo':'bar'}\n");
        assertFalseList.add("{'method': 'invalidMethod'}\n");
        assertFalseList.add("{'method': 'isPrime'}\n");
        assertFalseList.add("{'method': 'isPrime', 'number': 'foo'}\n");
        for (var jsonString : assertFalseList) {
            assertFalse(RequestResponseObjectManipulation.isValidRequest(jsonString), jsonString);
        }


        assertTrueList.add("{'method': 'isPrime', 'number': 1}\n");
        assertTrueList.add("{'method': 'isPrime', 'number': 1.0}\n");
        assertTrueList.add("{'method': 'isPrime', 'number': 1.34e4}\n");
        for (var jsonString : assertTrueList) {
            assertTrue(RequestResponseObjectManipulation.isValidRequest(jsonString), jsonString);
        }
    }

    @Test
    void getNumberFromRequest() {
        assertNull(RequestResponseObjectManipulation.getNumberFromRequest("{}"));
        assertEquals(1,
                RequestResponseObjectManipulation.getNumberFromRequest(
                        "{'method': 'isPrime', 'number': 1}\n"
                )
        );
    }
}