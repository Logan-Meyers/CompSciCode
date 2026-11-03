package org.cpts422.parking.exceptions;

/** Thrown when `payCitation` gets an already paid citation (R9). */
public class CitationAlreadyPaidException extends RuntimeException {

    public CitationAlreadyPaidException(String message) {
        super(message);
    }
}
