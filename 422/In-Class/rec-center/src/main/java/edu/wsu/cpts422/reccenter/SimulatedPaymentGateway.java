package edu.wsu.cpts422.reccenter;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Stand-in for a real card processor, using gateway-style test cards:
 * 4242 4242 4242 4242 approved, 4000 0000 0000 0002 declined,
 * 4000 0000 0000 0119 times out. Any other 16-digit card is approved.
 */
@Component
public class SimulatedPaymentGateway implements PaymentGateway {

    static final String DECLINED_CARD = "4000000000000002";
    static final String TIMEOUT_CARD = "4000000000000119";

    @Override
    public ChargeResult charge(String cardNumber, long amountCents) {
        if (DECLINED_CARD.equals(cardNumber)) {
            return ChargeResult.declined("Card declined");
        }
        if (TIMEOUT_CARD.equals(cardNumber)) {
            pause(2_000);
            throw new GatewayTimeoutException("Payment gateway timed out");
        }
        return ChargeResult.approved("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }

    private void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
