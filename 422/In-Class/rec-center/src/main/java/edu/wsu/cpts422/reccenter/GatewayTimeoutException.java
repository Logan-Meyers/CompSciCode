package edu.wsu.cpts422.reccenter;

public class GatewayTimeoutException extends PaymentException {

    public GatewayTimeoutException(String message) {
        super(message);
    }
}
