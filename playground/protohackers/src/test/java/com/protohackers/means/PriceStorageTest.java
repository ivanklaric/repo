package com.protohackers.means;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

class PriceStorageTest {
    @Test
    public void testExampleFromTheAssignment() {
        var priceStorage = new PriceStorage();
        priceStorage.storePrice(new Message(new byte[] {0x49, 0x00, 0x00, 0x30, 0x39, 0x00, 0x00, 0x00, 0x65}));
        priceStorage.storePrice(new Message(new byte[] {0x49, 0x00, 0x00, 0x30, 0x3a, 0x00, 0x00, 0x00, 0x66}));
        priceStorage.storePrice(new Message(new byte[] {0x49, 0x00, 0x00, 0x30, 0x3b, 0x00, 0x00, 0x00, 0x64}));
        priceStorage.storePrice(new Message(new byte[] {0x49, 0x00, 0x00, (byte) 0xa0, 0x00, 0x00, 0x00, 0x00, 0x05}));
        assertEquals(101, priceStorage.getMeanPrice(12288, 16384));
    }

    @Test
    @Timeout(10)
    public void test100kMessages() {
        var priceStorage = new PriceStorage();
        int minTime = Integer.MAX_VALUE;
        int maxTime = 0;
        int totalPrice = 0;
        for (int i = 0; i < 100000; i++) {
            var msg = new Message(new byte[] {0x49,
                    0x00, 0x00, (byte) (Math.random() * 255), (byte) (Math.random() * 255),
                    0x00, 0x00, 0x00, (byte) (Math.random() * 255)});
            if (msg.getTimestamp() > maxTime)
                maxTime = msg.getTimestamp();
            if (msg.getTimestamp() < minTime)
                minTime = msg.getTimestamp();
            totalPrice += msg.getPrice();
            priceStorage.storePrice(msg);
        }
        assertEquals(totalPrice / 100000, priceStorage.getMeanPrice(minTime, maxTime));
    }

    @Test
    public void testEmptyStorage() {
        var priceStorage = new PriceStorage();
        // we should return zero if no prices are found in the rage
        assertEquals(0, priceStorage.getMeanPrice(100, 102));
        // we should return zero if min > max
        assertEquals(0, priceStorage.getMeanPrice(200, 100));
    }

}