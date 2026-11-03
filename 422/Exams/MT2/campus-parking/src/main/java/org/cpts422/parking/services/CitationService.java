package org.cpts422.parking.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.cpts422.parking.entities.Citation;
import org.cpts422.parking.entities.User;
import org.cpts422.parking.exceptions.CitationAlreadyPaidException;
import org.cpts422.parking.exceptions.CitationNotFoundException;
import org.cpts422.parking.exceptions.PaymentDeclinedException;
import org.cpts422.parking.exceptions.UserNotFoundException;
import org.cpts422.parking.integrations.DateProvider;
import org.cpts422.parking.integrations.NotificationService;
import org.cpts422.parking.integrations.PaymentGateway;
import org.cpts422.parking.repositories.CitationRepository;
import org.cpts422.parking.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Issues and collects parking citations. */
@Service
public class CitationService {

    private final UserRepository userRepository;
    private final CitationRepository citationRepository;
    private final PaymentGateway paymentGateway;
    private final NotificationService notificationService;
    private final DateProvider dateProvider;

    public CitationService(UserRepository userRepository,
                           CitationRepository citationRepository,
                           PaymentGateway paymentGateway,
                           NotificationService notificationService,
                           DateProvider dateProvider) {
        this.userRepository = userRepository;
        this.citationRepository = citationRepository;
        this.paymentGateway = paymentGateway;
        this.notificationService = notificationService;
        this.dateProvider = dateProvider;
    }

    /** Applies R6 to R8. */
    @Transactional
    public Citation issueCitation(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("R7: user not found: id " + userId));
        LocalDate today = dateProvider.today();

        Citation previous = null;
        for (Citation citation : citationRepository.findByUserId(userId)) {
            if (!citation.isPaid()
                    && (previous == null || citation.getIssuedDate().isAfter(previous.getIssuedDate()))) {
                previous = citation;
            }
        }

        int fine;
        if (previous == null) {
            fine = 2500;
        } else {
            long days = ChronoUnit.DAYS.between(previous.getIssuedDate(), today);
            if (days >= 15) {
                fine = 2500;
            } else if (previous.getFineCents() == 2500) {
                fine = 5000;
            } else {
                fine = 10000;
            }
        }

        Citation citation = citationRepository.save(new Citation(user, today, fine));
        notificationService.sendCitationNotice(user, citation);
        return citation;
    }

    /** Applies R9. */
    @Transactional
    public Citation payCitation(Long citationId) {
        Citation citation = citationRepository.findById(citationId)
                .orElseThrow(() -> new CitationNotFoundException(
                        "R9: citation not found: id " + citationId));
        if (citation.isPaid()) {
            throw new CitationAlreadyPaidException("R9: citation " + citationId + " is already paid");
        }
        if (!paymentGateway.charge(citationId, citation.getFineCents())) {
            throw new PaymentDeclinedException("R9: charge of " + citation.getFineCents()
                    + " cents declined for citation " + citationId);
        }
        citation.setPaid(true);
        return citationRepository.save(citation);
    }

    /** Returns every citation issued to the user, paid or unpaid. */
    @Transactional
    public List<Citation> findCitationsForUser(Long userId) {
        return citationRepository.findByUserId(userId);
    }
}
