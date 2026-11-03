package org.cpts422.parking.integrations;

import java.time.LocalDate;

/** Supplies today's date; services use this and never call {@code LocalDate.now()}. */
public interface DateProvider {

    LocalDate today();
}
