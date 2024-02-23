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
        for (int i = 0; i < 200000; i++) {
            if (i % 100 == 0)
                System.out.print(".");
            priceStorage.storePrice(new Message(new byte[] {0x49,  0x00, 0x00, (byte) (Math.random() * 255), (byte) (Math.random() * 255), 0x00, 0x65}));
        }
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