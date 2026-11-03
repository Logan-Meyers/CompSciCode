package org.cpts422.parking.integrations;

import org.cpts422.parking.entities.Citation;
import org.cpts422.parking.entities.Permit;
import org.cpts422.parking.entities.User;

/** Sends messages to users. */
public interface NotificationService {

    /** Sends a purchase confirmation after an approved permit charge (R5). */
    void sendPurchaseConfirmation(User user, Permit permit);

    /** Sends a citation notice with the fine for a newly issued citation (R8). */
    void sendCitationNotice(User user, Citation citation);
}
