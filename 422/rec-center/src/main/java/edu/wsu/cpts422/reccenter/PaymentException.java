package edu.wsu.cpts422.reccenter;

/** Base class for every payment problem the front desk should see. */
public class PaymentException extends RuntimeException {

    public PaymentException(String message) {
        super(message);
    }
}
