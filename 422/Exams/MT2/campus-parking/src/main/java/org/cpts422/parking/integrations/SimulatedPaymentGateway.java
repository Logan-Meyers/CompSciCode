package org.cpts422.parking.integrations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Simulated gateway that approves every charge and logs it. */
@Component
public class SimulatedPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(SimulatedPaymentGateway.class);

    @Override
    public boolean charge(Long userId, int amountCents) {
        log.info("Charged {} cents to user {}", amountCents, userId);
        return true;
    }
}
