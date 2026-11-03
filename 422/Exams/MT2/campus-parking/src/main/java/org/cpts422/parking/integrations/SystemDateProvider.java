package org.cpts422.parking.integrations;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

/** Date provider backed by the system clock; the only class that calls {@code LocalDate.now()}. */
@Component
public class SystemDateProvider implements DateProvider {

    @Override
    public LocalDate today() {
        return LocalDate.now();
    }
}
