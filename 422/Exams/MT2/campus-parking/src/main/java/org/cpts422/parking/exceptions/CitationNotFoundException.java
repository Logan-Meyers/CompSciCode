package org.cpts422.parking.exceptions;

/** Thrown when `payCitation` gets an unknown citation ID (R9). */
public class CitationNotFoundException extends RuntimeException {

    public CitationNotFoundException(String message) {
        super(message);
    }
}
