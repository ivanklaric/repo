package com.protohackers.prime;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PrimeNumberDetectorTest {
    @Test
    void isPrime() {
        Integer[] primes = {2, 3, 5, 7, 11, 13, 17, 19};
        Set<Integer> primesSet = new HashSet<>(Arrays.asList(primes));
        for (int i = 1; i <= 20; i++) {
            if (primesSet.contains(i)) {
                assertTrue(PrimeNumberDetector.isPrimeNumber(i), Integer.toString(i));
            } else {
                assertFalse(PrimeNumberDetector.isPrimeNumber(i), Integer.toString(i));
            }
        }
    }

}