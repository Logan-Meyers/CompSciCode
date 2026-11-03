package org.cpts422.parking.exceptions;

/** Thrown when a user ID is unknown (R4 step 1; also `issueCitation`). */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
