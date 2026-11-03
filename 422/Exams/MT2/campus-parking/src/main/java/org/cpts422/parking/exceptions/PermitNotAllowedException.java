package org.cpts422.parking.exceptions;

/** Thrown when a permit purchase fails R1, R2, or R3 (checked in R4 order). */
public class PermitNotAllowedException extends RuntimeException {

    public PermitNotAllowedException(String message) {
        super(message);
    }
}
