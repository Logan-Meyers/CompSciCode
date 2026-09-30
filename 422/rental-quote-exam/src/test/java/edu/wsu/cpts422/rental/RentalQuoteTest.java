package edu.wsu.cpts422.rental;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RentalQuoteTest {

    // ---------- Strong normal ECT (README Part A) ----------

    // Covers E1 (driverAge 25 to 99), E2 (days 1 to 30), E3 (ECONOMY)
    // Expected: 3 x (40 + 0) = 120
    @Test
    void test_E1_E2_E3() {
        assertEquals(120, RentalQuote.quote(40, 3, CarClass.ECONOMY));
    }

    // ---------- Robust BVT (README Part B) ----------
    // Nominal values for the inputs not being varied: driverAge 40, carClass ECONOMY

    // Covers B1 (days = 0, min-): outside the valid range, so it must be rejected
    @Test
    void test_B1_daysMinMinus() {
        assertThrows(IllegalArgumentException.class,
                () -> RentalQuote.quote(40, 0, CarClass.ECONOMY));
    }

    // Covers B2 (days = 1, min)
    // Expected: 1 x (40 + 0) = 40
    @Test
    void test_B2_daysMin() {
        assertEquals(40, RentalQuote.quote(40, 1, CarClass.ECONOMY));
    }

    // ---------- Write your tests below ----------

    // Covers E1 (driverAge 25 to 99), E2 (days 1 to 30), E5 (STANDARD)
    // Expected: 3 x (60 + 0) = 180
    @Test
    void test_E1_E2_E5() {
        assertEquals(180, RentalQuote.quote(40, 3, CarClass.STANDARD));
    }

    // Covers E1 (driverAge 25 to 99), E2 (days 1 to 30), E6 (PREMIUM)
    // Expected: 3 x (100 + 0) = 300
    @Test
    void test_E1_E2_E6() {
        assertEquals(300, RentalQuote.quote(40, 3, CarClass.PREMIUM));
    }

    // Covers E4 (driverAge 21 to 24), E2 (days 1 to 30), E3 (ECONOMY)
    // Expected: 3 x (40 + 15) = 165
    @Test
    void test_E4_E2_E3() {
        assertEquals(165, RentalQuote.quote(22, 3, CarClass.ECONOMY));
    }

    // Covers E4 (driverAge 21 to 24), E2 (days 1 to 30), E5 (STANDARD)
    // Expected: 3 x (60 + 15) = 225
    @Test
    void test_E4_E2_E5() {
        assertEquals(225, RentalQuote.quote(22, 3, CarClass.STANDARD));
    }

    // Covers E4 (driverAge 21 to 24), E2 (days 1 to 30), E6 (PREMIUM)
    // Drivers aged 21-24 cannot rent PREMIUM cars, so it must be rejected
    @Test
    void test_E4_E2_E6() {
        assertThrows(IllegalArgumentException.class,
                () -> RentalQuote.quote(22, 3, CarClass.PREMIUM));
    }

    // Covers B3 (days = 2, min+)
    // Expected: 2 x (40 + 0) = 80
    @Test
    void test_B3_daysMinPlus() {
        assertEquals(80, RentalQuote.quote(40, 2, CarClass.ECONOMY));
    }

    // Covers B4 (days = 29, max-)
    // Expected: 29 x (40 + 0) = 1160
    @Test
    void test_B4_daysMaxMinus() {
        assertEquals(1160, RentalQuote.quote(40, 29, CarClass.ECONOMY));
    }

    // Covers B5 (days = 30, max)
    // Expected: 30 x (40 + 0) = 1200
    @Test
    void test_B5_daysMax() {
        assertEquals(1200, RentalQuote.quote(40, 30, CarClass.ECONOMY));
    }

    // Covers B6 (days = 31, max+): outside the valid range, so it must be rejected
    @Test
    void test_B6_daysMaxPlus() {
        assertThrows(IllegalArgumentException.class,
                () -> RentalQuote.quote(40, 31, CarClass.ECONOMY));
    }

    // Covers B7 (driverAge = 20, min- for range 21 to 24): outside the valid range, so it must be rejected
    @Test
    void test_B7_driverAge21to24MinMinus() {
        assertThrows(IllegalArgumentException.class,
                () -> RentalQuote.quote(20, 3, CarClass.ECONOMY));
    }

    // Covers B8 (driverAge = 21, min for range 21 to 24)
    // Expected: 3 x (40 + 15) = 165
    @Test
    void test_B8_driverAge21to24Min() {
        assertEquals(165, RentalQuote.quote(21, 3, CarClass.ECONOMY));
    }

    // Covers B9 (driverAge = 22, min+ for range 21 to 24)
    // Expected: 3 x (40 + 15) = 165
    @Test
    void test_B9_driverAge21to24MinPlus() {
        assertEquals(165, RentalQuote.quote(22, 3, CarClass.ECONOMY));
    }

    // Covers B10 (driverAge = 23, max- for range 21 to 24)
    // Expected: 3 x (40 + 15) = 165
    @Test
    void test_B10_driverAge21to24MaxMinus() {
        assertEquals(165, RentalQuote.quote(23, 3, CarClass.ECONOMY));
    }

    // Covers B11 (driverAge = 24, max for range 21 to 24)
    // Expected: 3 x (40 + 15) = 165
    @Test
    void test_B11_driverAge21to24Max() {
        assertEquals(165, RentalQuote.quote(24, 3, CarClass.ECONOMY));
    }

    // Covers B12 (driverAge = 25, max+ for range 21 to 24)
    // Expected: 3 x (40 + 0) = 120
    @Test
    void test_B12_driverAge21to24MaxPlus() {
        assertEquals(120, RentalQuote.quote(25, 3, CarClass.ECONOMY));
    }

    // Covers B13 (driverAge = 24, min- for range 25 to 99)
    // Expected: 3 x (40 + 15) = 165
    @Test
    void test_B13_driverAge25to99MinMinus() {
        assertEquals(165, RentalQuote.quote(24, 3, CarClass.ECONOMY));
    }

    // Covers B14 (driverAge = 25, min for range 25 to 99)
    // Expected: 3 x (40 + 0) = 120
    @Test
    void test_B14_driverAge25to99Min() {
        assertEquals(120, RentalQuote.quote(25, 3, CarClass.ECONOMY));
    }

    // Covers B15 (driverAge = 26, min+ for range 25 to 99)
    // Expected: 3 x (40 + 0) = 120
    @Test
    void test_B15_driverAge25to99MinPlus() {
        assertEquals(120, RentalQuote.quote(26, 3, CarClass.ECONOMY));
    }

    // Covers B16 (driverAge = 98, max- for range 25 to 99)
    // Expected: 3 x (40 + 0) = 120
    @Test
    void test_B16_driverAge25to99MaxMinus() {
        assertEquals(120, RentalQuote.quote(98, 3, CarClass.ECONOMY));
    }

    // Covers B17 (driverAge = 99, max for range 25 to 99)
    // Expected: 3 x (40 + 0) = 120
    @Test
    void test_B17_driverAge25to99Max() {
        assertEquals(120, RentalQuote.quote(99, 3, CarClass.ECONOMY));
    }

    // Covers B18 (driverAge = 100, max+ for range 25 to 99): outside the valid range, so it must be rejected
    @Test
    void test_B18_driverAge25to99MaxPlus() {
        assertThrows(IllegalArgumentException.class,
                () -> RentalQuote.quote(100, 3, CarClass.ECONOMY));
    }

}
