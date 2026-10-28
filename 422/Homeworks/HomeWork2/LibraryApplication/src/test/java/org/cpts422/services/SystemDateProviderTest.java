package org.cpts422.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SystemDateProviderTest {

    @Test
    @DisplayName("Gets the current date from the provider and checks it against the expected date of today")
    void todayTest() {
        LocalDate before = LocalDate.now();
        LocalDate actual = new SystemDateProvider().today();
        LocalDate after = LocalDate.now();
        assertFalse(actual.isBefore(before));
        assertFalse(actual.isAfter(after));
    }
}