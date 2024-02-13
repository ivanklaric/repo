package com.protohackers.prime;

public class PrimeNumberDetector {
    public static boolean isPrimeNumber(Number num) {
        if (num.floatValue() - num.intValue() > 0) {
            // floats can't be primes.
            return false;
        }
        if (num.equals(1))
            return false;

        // TODO: this is a naive implementation, make it faster
        var n = num.intValue();
        for (int i = 2; i < n; i++) {
            if ((n % i) == 0) {
                return false;
            }
        }
        return true;
    }
}
