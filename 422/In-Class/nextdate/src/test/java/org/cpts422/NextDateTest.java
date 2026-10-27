package org.cpts422;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NextDateTest {
    private NextDate nextDate;

    @BeforeEach
    void setUp() {
        nextDate = new NextDate();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @DisplayName("Test mid month")
    void midMonth() {
        assertEquals("6/16/2026", nextDate.nextDate(6, 15, 2026));
    }

    @ParameterizedTest
    @DisplayName("Test end of 31-day month")
    @ValueSource(ints = {1, 3, 5, 7, 8, 10})
    void endOf31DayMonth(int month) {
        assertEquals((month+1)+"/1/2026", nextDate.nextDate(month, 31, 2026));
    }

    @Test
    @DisplayName("Test end of year")
    void endOfYear() {
        assertEquals("1/1/2027", nextDate.nextDate(12, 31, 2026));
    }

    @ParameterizedTest
    @CsvSource({"2,28,2026,3/1/2026", "2,29,2024,3/1/2024", "2,28,2000,2/29/2000", "2,28,1900,3/1/1900"})
    @DisplayName("Test end of Feb")
    void endOfFeb(int month, int day, int year, String expected) {
        assertEquals(expected, nextDate.nextDate(month, day, year));
    }

    @ParameterizedTest
    @DisplayName("Test for invalid year too small or big")
    @ValueSource(ints = {1811, 2213})
    void invalidYear(int year) {
        assertThrows(IllegalArgumentException.class, () -> nextDate.nextDate(3, 14, year));
    }


    @ParameterizedTest
    @DisplayName("Test for invalid months too small or big")
    @ValueSource(ints = {-1, 0, 13, 100})
    void invalidMonth(int month) {
        assertThrows(IllegalArgumentException.class, () -> nextDate.nextDate(month, 14, 2000));
    }
}
