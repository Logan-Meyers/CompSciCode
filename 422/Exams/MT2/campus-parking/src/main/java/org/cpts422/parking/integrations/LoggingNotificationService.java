package org.cpts422.parking.integrations;

import org.cpts422.parking.entities.Citation;
import org.cpts422.parking.entities.Permit;
import org.cpts422.parking.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Notification service that logs each message instead of sending it. */
@Component
public class LoggingNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationService.class);

    @Override
    public void sendPurchaseConfirmation(User user, Permit permit) {
        log.info("Purchase confirmation to {}: {} permit, {} cents paid on {}",
                user.getName(), permit.getZone(), permit.getPricePaidCents(), permit.getPurchaseDate());
    }

    @Override
    public void sendCitationNotice(User user, Citation citation) {
        log.info("Citation notice to {}: fine of {} cents issued on {}",
                user.getName(), citation.getFineCents(), citation.getIssuedDate());
    }
}
