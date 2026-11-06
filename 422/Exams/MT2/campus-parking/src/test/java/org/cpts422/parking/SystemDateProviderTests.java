package org.cpts422.parking;

import org.cpts422.parking.integrations.SystemDateProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;

// no Mockito here: SystemDateProvider has no collaborators to mock
class SystemDateProviderTests {

    @Test
    @DisplayName("Gets the current date from the provider and checks it against the expected date of today")
    void todayTest() {
        // sandwich the call between two reads of the clock so a midnight rollover can't fail the test
        LocalDate before = LocalDate.now();
        LocalDate actual = new SystemDateProvider().today();
        LocalDate after = LocalDate.now();

        assertFalse(actual.isBefore(before));
        assertFalse(actual.isAfter(after));
    }
}
