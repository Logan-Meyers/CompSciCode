package org.cpts422;

import org.junit.jupiter.api.Test;
import org.cpts422.Arithmetic;

import static org.junit.jupiter.api.Assertions.*;

class ArithmeticTest {
    // Task: write normal & robust boundary tests for the methods sum(), difference() and quotient()
    // You may or may not use setup and teardown methods.

    @Test
    void sum() {
        // normal
        assertEquals(2, Arithmetic.sum(1, 1));        // both min
        assertEquals(400, Arithmetic.sum(200, 200));  // both max
        assertEquals(201, Arithmetic.sum(1, 200));    // one min one max
        assertEquals(201, Arithmetic.sum(2, 199));    // both a little inside range
        assertEquals(200, Arithmetic.sum(100, 100));  // both in middle of range

        // robust (assumes the methods don't throw but should act as normal even outside of range)
        assertEquals(50, Arithmetic.sum(0, 50));      // one below range
        assertEquals(500, Arithmetic.sum(250, 250));  // both above range
    }

    @Test
    void difference() {
        // normal
        assertEquals(0, Arithmetic.difference(1, 1));       // both min
        assertEquals(0, Arithmetic.difference(200, 200));   // both max
        assertEquals(-199, Arithmetic.difference(1, 200));  // one min one max
        assertEquals(196, Arithmetic.difference(199, 3));   // both a little inside range
        assertEquals(2, Arithmetic.difference(101, 99));    // both in middle of range

        // robust (assumes the methods don't throw but should act as normal even outside of range)
        assertEquals(-50, Arithmetic.difference(0, 50));      // one below range
        assertEquals(250, Arithmetic.difference(500, 250));  // both above range
    }

    @Test
    void quotient() {
        // normal
        assertEquals(1, Arithmetic.quotient(1, 1));      // both min
        assertEquals(1, Arithmetic.quotient(200, 200));  // both max
        assertEquals(200, Arithmetic.quotient(200, 1));  // max/min
        assertEquals(0, Arithmetic.quotient(1, 200));    // min/max - int truncation
        assertEquals(99, Arithmetic.quotient(199, 2));   // both just inside range - int truncation
        assertEquals(1, Arithmetic.quotient(100, 100));  // both in middle of range

        // robust (assumes the methods don't throw but should act as normal even outside of range)
        assertEquals(5, Arithmetic.quotient(-250, -50));   // both below range
        assertEquals(-10, Arithmetic.quotient(50, -5));     // one in range/one below range
        assertEquals(5, Arithmetic.quotient(250, 50));      // one above/one in range
        assertEquals(8, Arithmetic.quotient(25600, 3200));  // both above range
    }
}