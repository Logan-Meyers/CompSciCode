package edu.wsu.cpts422.reccenter;

/** The outside card processor. In production this would call a bank over the network. */
public interface PaymentGateway {

    /**
     * Charges the card.
     *
     * @throws GatewayTimeoutException if the gateway does not answer in time
     */
    ChargeResult charge(String cardNumber, long amountCents);
}
