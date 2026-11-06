package org.cpts422.parking.services;

import java.util.Optional;

import org.cpts422.parking.entities.Citation;
import org.cpts422.parking.entities.Permit;
import org.cpts422.parking.entities.User;
import org.cpts422.parking.entities.UserType;
import org.cpts422.parking.entities.Zone;
import org.cpts422.parking.exceptions.PaymentDeclinedException;
import org.cpts422.parking.exceptions.PermitNotAllowedException;
import org.cpts422.parking.exceptions.UserNotFoundException;
import org.cpts422.parking.integrations.DateProvider;
import org.cpts422.parking.integrations.NotificationService;
import org.cpts422.parking.integrations.PaymentGateway;
import org.cpts422.parking.repositories.CitationRepository;
import org.cpts422.parking.repositories.PermitRepository;
import org.cpts422.parking.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Sells parking permits. */
@Service
public class PermitService {

    private final UserRepository userRepository;
    private final PermitRepository permitRepository;
    private final CitationRepository citationRepository;
    private final PaymentGateway paymentGateway;
    private final NotificationService notificationService;
    private final DateProvider dateProvider;

    public PermitService(UserRepository userRepository,
                         PermitRepository permitRepository,
                         CitationRepository citationRepository,
                         PaymentGateway paymentGateway,
                         NotificationService notificationService,
                         DateProvider dateProvider) {
        this.userRepository = userRepository;
        this.permitRepository = permitRepository;
        this.citationRepository = citationRepository;
        this.paymentGateway = paymentGateway;
        this.notificationService = notificationService;
        this.dateProvider = dateProvider;
    }

    /** Applies R1 to R5, with the checks in the R4 order. */
    @Transactional
    public Permit purchasePermit(Long userId, Zone zone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("R4: user not found: id " + userId));

        int price = priceForOrThrow(user, userId, zone);

        if (!permitRepository.findByUserId(userId).isEmpty()) {
            throw new PermitNotAllowedException("R2: user " + userId + " already holds a permit");
        }

        for (Citation citation : citationRepository.findByUserId(userId)) {
            if (!citation.isPaid()) {
                throw new PermitNotAllowedException(
                        "R3: user " + userId + " has unpaid citation " + citation.getId());
            }
        }

        if (!paymentGateway.charge(userId, price)) {
            throw new PaymentDeclinedException(
                    "R5: charge of " + price + " cents declined for user " + userId);
        }

        Permit permit = permitRepository.save(new Permit(user, zone, price, dateProvider.today()));
        notificationService.sendPurchaseConfirmation(user, permit);
        return permit;
    }

    /** Applies R1: returns the fixed price in cents for the user's type and the zone, or throws if the zone isn't allowed. */
    private int priceForOrThrow(User user, Long userId, Zone zone) {
        if (user.getType() == UserType.STUDENT && zone == Zone.GREEN) {
            return 6000;
        } else if (user.getType() == UserType.STUDENT && zone == Zone.BLUE) {
            return 18000;
        } else if (user.getType() == UserType.EMPLOYEE && zone == Zone.GREEN) {
            return 9000;
        } else if (user.getType() == UserType.EMPLOYEE && zone == Zone.RED) {
            return 24000;
        }
        throw new PermitNotAllowedException(
                "R1: zone " + zone + " is not allowed for " + user.getType() + " (user " + userId + ")");
    }

    /** Returns the user's permit, if any. */
    @Transactional
    public Optional<Permit> findPermitForUser(Long userId) {
        return permitRepository.findByUserId(userId).stream().findFirst();
    }
}
