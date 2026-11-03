package edu.wsu.cpts422.gym;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GymBillingTest {

    // ---------- One test per rule of the decision table (README Part A) ----------

    // Covers R1 (age 13 to 17, BASIC), using age 15 and 6 months
    // Expected: 6 x 20 = 120
    @Test
    void test_R1() {
        assertEquals(120, GymBilling.price(15, 6, Plan.BASIC));
    }

    // Covers R2 (age 13 to 17, PLUS), using age 15 and 6 months
    // Expected: 6 x 40 = 240
    @Test
    void test_R2() {
        assertEquals(240, GymBilling.price(15, 6, Plan.PLUS));
    }

    // Covers R3 (age 13 to 17, ELITE), using age 15 and 6 months
    // Expected: not allowed, throws IllegalArgumentException
    @Test
    void test_R3() {
        assertThrows(IllegalArgumentException.class,
                () -> GymBilling.price(15, 6, Plan.ELITE));
    }

    // Covers R4 (age 18 to 99, BASIC), using age 56 and 6 months
    // Expected: 6 x 30 = 180
    @Test
    void test_R4() {
        assertEquals(180, GymBilling.price(56, 6, Plan.BASIC));
    }

    // Covers R5 (age 18 to 99, PLUS), using age 56 and 6 months
    // Expected: 6 x 50 = 300
    @Test
    void test_R5() {
        assertEquals(300, GymBilling.price(56, 6, Plan.PLUS));
    }

    // Covers R6 (age 18 to 99, ELITE), using age 56 and 6 months
    // Expected: 6 x 80 = 480
    @Test
    void test_R6() {
        assertEquals(480, GymBilling.price(56, 6, Plan.ELITE));
    }

    // ---------- Robust BVT (README Part B) ----------
    // Nominal values for the inputs not being varied: memberAge 56 (nom), months 6 (nom), plan BASIC

    // Covers B1 (months = 0, min-): outside the valid range, so it must be rejected
    @Test
    void test_B1_monthsMinMinus() {
        assertThrows(IllegalArgumentException.class,
                () -> GymBilling.price(56, 0, Plan.BASIC));
    }

    // Covers B2 (months = 1, min)
    // Expected: 1 x 30 = 30
    @Test
    void test_B2_monthsMin() {
        assertEquals(30, GymBilling.price(56, 1, Plan.BASIC));
    }

    // Covers B3 (months = 2, min+)
    // Expected: 2 x 30 = 60
    @Test
    void test_B3_monthsMinPlus() {
        assertEquals(60, GymBilling.price(56, 2, Plan.BASIC));
    }

    // Covers B4 (months = 6, nom)
    // Expected: 6 x 30 = 180
    @Test
    void test_B4_monthsNom() {
        assertEquals(180, GymBilling.price(56, 6, Plan.BASIC));
    }

    // Covers B5 (months = 11, max-)
    // Expected: 11 x 30 = 330
    @Test
    void test_B5_monthsMaxMinus() {
        assertEquals(330, GymBilling.price(56, 11, Plan.BASIC));
    }

    // Covers B6 (months = 12, max)
    // Expected: 12 x 30 = 360
    @Test
    void test_B6_monthsMax() {
        assertEquals(360, GymBilling.price(56, 12, Plan.BASIC));
    }

    // Covers B7 (months = 13, max+): outside the valid range, so it must be rejected
    @Test
    void test_B7_monthsMaxPlus() {
        assertThrows(IllegalArgumentException.class,
                () -> GymBilling.price(56, 13, Plan.BASIC));
    }

    // Covers B8 (memberAge = 12, min-): outside the valid range, so it must be rejected
    @Test
    void test_B8_memberAgeMinMinus() {
        assertThrows(IllegalArgumentException.class,
                () -> GymBilling.price(12, 6, Plan.BASIC));
    }

    // Covers B9 (memberAge = 13, min)
    // Expected: 6 x 20 = 120
    @Test
    void test_B9_memberAgeMin() {
        assertEquals(120, GymBilling.price(13, 6, Plan.BASIC));
    }

    // Covers B10 (memberAge = 14, min+)
    // Expected: 6 x 20 = 120
    @Test
    void test_B10_memberAgeMinPlus() {
        assertEquals(120, GymBilling.price(14, 6, Plan.BASIC));
    }

    // Covers B11 (memberAge = 56, nom)
    // Expected: 6 x 30 = 180
    @Test
    void test_B11_memberAgeNom() {
        assertEquals(180, GymBilling.price(56, 6, Plan.BASIC));
    }

    // Covers B12 (memberAge = 98, max-)
    // Expected: 6 x 30 = 180
    @Test
    void test_B12_memberAgeMaxMinus() {
        assertEquals(180, GymBilling.price(98, 6, Plan.BASIC));
    }

    // Covers B13 (memberAge = 99, max)
    // Expected: 6 x 30 = 180
    @Test
    void test_B13_memberAgeMax() {
        assertEquals(180, GymBilling.price(99, 6, Plan.BASIC));
    }

    // Covers B14 (memberAge = 100, max+): outside the valid range, so it must be rejected
    @Test
    void test_B14_memberAgeMaxPlus() {
        assertThrows(IllegalArgumentException.class,
                () -> GymBilling.price(100, 6, Plan.BASIC));
    }

}
