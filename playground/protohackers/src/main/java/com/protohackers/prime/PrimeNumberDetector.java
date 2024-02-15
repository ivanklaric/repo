package com.protohackers.prime;

import java.math.BigInteger;

public class PrimeNumberDetector {
    public static boolean isPrimeNumber(Number num) {
        if (num.intValue() < 0) {
            return false;
        }
        if (num.floatValue() - (float)num.intValue() > 0.0) {
            // floats can't be primes.
            return false;
        }
        var bigInt = BigInteger.valueOf(num.longValue());
        return bigInt.isProbablePrime(100);
    }
}
