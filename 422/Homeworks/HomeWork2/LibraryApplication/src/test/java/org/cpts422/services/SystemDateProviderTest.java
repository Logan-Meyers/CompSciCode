package org.cpts422.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
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