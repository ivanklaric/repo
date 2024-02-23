package com.protohackers.means;

import org.junit.jupiter.api.Test;

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

}