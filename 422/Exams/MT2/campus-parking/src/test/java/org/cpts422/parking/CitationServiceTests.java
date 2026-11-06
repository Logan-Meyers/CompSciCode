package org.cpts422.parking;

import org.cpts422.parking.entities.Citation;
import org.cpts422.parking.entities.User;
import org.cpts422.parking.entities.UserType;
import org.cpts422.parking.exceptions.CitationAlreadyPaidException;
import org.cpts422.parking.exceptions.CitationNotFoundException;
import org.cpts422.parking.exceptions.PaymentDeclinedException;
import org.cpts422.parking.exceptions.UserNotFoundException;
import org.cpts422.parking.integrations.DateProvider;
import org.cpts422.parking.integrations.NotificationService;
import org.cpts422.parking.integrations.PaymentGateway;
import org.cpts422.parking.repositories.CitationRepository;
import org.cpts422.parking.repositories.UserRepository;
import org.cpts422.parking.services.CitationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CitationServiceTests {

    // the user and citation ids are different on purpose, so charging the wrong one gets caught
    private static final long USER_ID = 1L;
    private static final long CITATION_ID = 7L;
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 25);

    @Mock private UserRepository userRepository;
    @Mock private CitationRepository citationRepository;
    @Mock private PaymentGateway paymentGateway;
    @Mock private NotificationService notificationService;
    @Mock private DateProvider dateProvider;

    @InjectMocks private CitationService citationService;

    private User user;

    @BeforeEach
    void createUser() {
        user = new User("Bob", UserType.STUDENT, "BOB-123");
        // ids are normally given out by the database, so set it by hand
        ReflectionTestUtils.setField(user, "id", USER_ID);
    }

    // matches the one citation we expect to be saved: today's date, the right fine, and unpaid
    private Citation citationMatching(int fineCents) {
        return argThat(citation -> citation.getUser() == user
                && TODAY.equals(citation.getIssuedDate())
                && citation.getFineCents() == fineCents
                && !citation.isPaid());
    }

    private Citation previousCitation(int fineCents, int daysAgo, boolean paid) {
        Citation previous = new Citation(user, TODAY.minusDays(daysAgo), fineCents);
        previous.setPaid(paid);
        return previous;
    }

    // everything issueCitation needs except the previous citations and the save
    private void stubUserAndToday(List<Citation> previousCitations) {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(dateProvider.today()).thenReturn(TODAY);
        when(citationRepository.findByUserId(USER_ID)).thenReturn(previousCitations);
    }

    /* ---------- R6, R7, R8 - Issuing a Citation ---------- */

    @Test
    @DisplayName("R7 - A user with no previous citation should get the first fine step")
    void R7_firstCitationShouldBeFirstStep() {
        Citation saved = new Citation(user, TODAY, 2500);
        stubUserAndToday(List.of());
        when(citationRepository.save(citationMatching(2500))).thenReturn(saved);

        Citation result = citationService.issueCitation(USER_ID);

        assertSame(saved, result);
        verify(citationRepository).save(citationMatching(2500));
    }

    @ParameterizedTest(name = "R7 - previous fine {0} issued {1} days ago should give a fine of {2}")
    @CsvSource({
            "2500,  15, 5000",   // 15 days is still "15 days or fewer"
            "2500,  1,  5000",
            "5000,  4,  10000",
            "10000, 5,  10000",  // already at the last step
            "2500,  16, 2500",   // 16 days is too long ago, back to the first step
            "10000, 16, 2500"
    })
    void R7_repeatCitationShouldMoveByDaysSincePrevious(int previousFine, int daysAgo, int expectedFine) {
        Citation saved = new Citation(user, TODAY, expectedFine);
        stubUserAndToday(List.of(previousCitation(previousFine, daysAgo, false)));
        when(citationRepository.save(citationMatching(expectedFine))).thenReturn(saved);

        Citation result = citationService.issueCitation(USER_ID);

        assertSame(saved, result);
        verify(citationRepository).save(citationMatching(expectedFine));
    }

    @Test
    @DisplayName("R7 - A previous citation that was already paid should still raise the fine")
    void R7_paidPreviousCitationShouldStillCount() {
        Citation saved = new Citation(user, TODAY, 5000);
        stubUserAndToday(List.of(previousCitation(2500, 3, true)));
        when(citationRepository.save(citationMatching(5000))).thenReturn(saved);

        Citation result = citationService.issueCitation(USER_ID);

        assertSame(saved, result);
        verify(citationRepository).save(citationMatching(5000));
    }

    @Test
    @DisplayName("R7 - Only the most recent previous citation should decide the fine when they are listed newest first")
    void R7_mostRecentPreviousCitationShouldDecideFineWhenListedNewestFirst() {
        // listed newest first: the newest is 2500 from 3 days ago, so the new fine is 5000
        // if the older one (10000 from 10 days ago) was used by mistake, the fine would stay at 10000
        Citation saved = new Citation(user, TODAY, 5000);
        stubUserAndToday(List.of(previousCitation(2500, 3, false), previousCitation(10000, 10, false)));
        when(citationRepository.save(citationMatching(5000))).thenReturn(saved);

        Citation result = citationService.issueCitation(USER_ID);

        assertSame(saved, result);
        verify(citationRepository).save(citationMatching(5000));
    }

    @Test
    @DisplayName("R7 - Only the most recent previous citation should decide the fine when they are listed oldest first")
    void R7_mostRecentPreviousCitationShouldDecideFineWhenListedOldestFirst() {
        // same citations as the test above, in the opposite order: the loop has to swap out the older
        // citation it saw first for the newer one (this is the isAfter == true branch)
        Citation saved = new Citation(user, TODAY, 5000);
        stubUserAndToday(List.of(previousCitation(10000, 10, false), previousCitation(2500, 3, false)));
        when(citationRepository.save(citationMatching(5000))).thenReturn(saved);

        Citation result = citationService.issueCitation(USER_ID);

        assertSame(saved, result);
        verify(citationRepository).save(citationMatching(5000));
    }

    @Test
    @DisplayName("R8 - A new citation should send a notice with the saved citation to the user")
    void R8_issuedCitationShouldSendNoticeWithSavedCitation() {
        Citation saved = new Citation(user, TODAY, 2500);
        stubUserAndToday(List.of());
        when(citationRepository.save(citationMatching(2500))).thenReturn(saved);

        citationService.issueCitation(USER_ID);

        verify(citationRepository).save(citationMatching(2500));
        verify(notificationService).sendCitationNotice(user, saved);
    }

    @Test
    @DisplayName("R8 - Issuing a citation for a user that doesn't exist should error and save and send nothing")
    void R8_missingUserShouldError() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> citationService.issueCitation(USER_ID));

        verifyNoInteractions(citationRepository, notificationService);
    }

    /* ---------- R9 - Paying a Citation ---------- */

    @Test
    @DisplayName("R9 - Paying a citation that doesn't exist should error and charge nothing")
    void R9_unknownCitationShouldError() {
        when(citationRepository.findById(CITATION_ID)).thenReturn(Optional.empty());

        assertThrows(CitationNotFoundException.class, () -> citationService.payCitation(CITATION_ID));

        verifyNoInteractions(paymentGateway);
    }

    @Test
    @DisplayName("R9 - Paying a citation that is already paid should error and charge nothing")
    void R9_alreadyPaidCitationShouldError() {
        Citation paid = previousCitation(2500, 3, true);
        when(citationRepository.findById(CITATION_ID)).thenReturn(Optional.of(paid));

        assertThrows(CitationAlreadyPaidException.class, () -> citationService.payCitation(CITATION_ID));

        verifyNoInteractions(paymentGateway);
        verify(citationRepository, never()).save(paid);
    }

    @Test
    @DisplayName("R9 - An approved charge should charge the citation's user its fine, then mark it paid, save it and return it")
    void R9_approvedChargeShouldMarkPaidSaveAndReturn() {
        Citation citation = previousCitation(5000, 3, false);
        when(citationRepository.findById(CITATION_ID)).thenReturn(Optional.of(citation));
        when(paymentGateway.charge(USER_ID, 5000)).thenReturn(true);
        when(citationRepository.save(citation)).thenReturn(citation);

        Citation result = citationService.payCitation(CITATION_ID);

        assertSame(citation, result);
        assertTrue(result.isPaid());
        verify(paymentGateway).charge(USER_ID, 5000);
        verify(citationRepository).save(citation);
    }

    @Test
    @DisplayName("R9 - A declined charge should error, leave the citation unpaid and not save it")
    void R9_declinedChargeShouldLeaveCitationUnpaid() {
        Citation citation = previousCitation(5000, 3, false);
        when(citationRepository.findById(CITATION_ID)).thenReturn(Optional.of(citation));
        when(paymentGateway.charge(USER_ID, 5000)).thenReturn(false);

        assertThrows(PaymentDeclinedException.class, () -> citationService.payCitation(CITATION_ID));

        assertFalse(citation.isPaid());
        verify(paymentGateway).charge(USER_ID, 5000);
        verify(citationRepository, never()).save(citation);
    }

    /* ---------- R12 - Finding a User's Citations ---------- */

    @Test
    @DisplayName("R12 - Getting a user's citations should return both the paid and the unpaid ones")
    void R12_userCitationsShouldIncludePaidAndUnpaid() {
        List<Citation> citations = List.of(previousCitation(2500, 20, true), previousCitation(2500, 3, false));
        when(citationRepository.findByUserId(USER_ID)).thenReturn(citations);

        assertEquals(citations, citationService.findCitationsForUser(USER_ID));
    }
}
