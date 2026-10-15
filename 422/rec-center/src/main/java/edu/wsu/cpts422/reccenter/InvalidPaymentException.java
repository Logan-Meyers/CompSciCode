package edu.wsu.cpts422.reccenter;

public class InvalidPaymentException extends PaymentException {

    public InvalidPaymentException(String message) {
        super(message);
    }
}
