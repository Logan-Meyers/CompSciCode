package org.cpts422;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TriangleTest {

    @BeforeEach
    void setUp() {
        System.out.println("setup call");
    }

    @AfterEach
    void tearDown() {
        System.out.println("teardown call");
    }

    @Test
    void isValid() {
        assertFalse(Triangle.isValid(0, 2, 3));
        assertTrue(Triangle.isValid(1, 2, 3));
    }

    @Test
    void isEquilateral() {
    }
}