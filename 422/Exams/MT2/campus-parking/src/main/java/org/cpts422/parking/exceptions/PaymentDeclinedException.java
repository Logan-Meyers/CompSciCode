package org.cpts422.parking.exceptions;

/** Thrown when the payment gateway declines a charge (R5, R9). */
public class PaymentDeclinedException extends RuntimeException {

    public PaymentDeclinedException(String message) {
        super(message);
    }
}
