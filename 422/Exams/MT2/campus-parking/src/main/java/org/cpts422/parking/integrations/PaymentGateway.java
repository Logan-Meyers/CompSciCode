package org.cpts422.parking.integrations;

/** Charges the payment card a user saved at registration; card details never reach the services. */
public interface PaymentGateway {

    /**
     * Charges the user's card on file (R5, R9).
     *
     * @param userId      the user to charge
     * @param amountCents the amount in whole cents
     * @return true if the charge was approved, false if it was declined
     */
    boolean charge(Long userId, int amountCents);
}
