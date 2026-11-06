package org.cpts422.parking;

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
import org.cpts422.parking.services.PermitService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PermitServiceTests {

    private static final long USER_ID = 1L;
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 3);

    // PermitService takes all of these through its constructor, so they all have to be here
    // even though the R11 tests only use the permit repository
    @Mock private UserRepository userRepository;
    @Mock private PermitRepository permitRepository;
    @Mock private CitationRepository citationRepository;
    @Mock private PaymentGateway paymentGateway;
    @Mock private NotificationService notificationService;
    @Mock private DateProvider dateProvider;

    @InjectMocks private PermitService permitService;

    // real objects instead of mocks: Permit is just data, and Zone is an enum (nothing to mock)
    private static Permit permitFor(Zone zone, int priceCents) {
        User student = new User("Bob", UserType.STUDENT, "BOB-123");
        return new Permit(student, zone, priceCents, TODAY);
    }

    // matches the one permit we expect to be saved, field by field (Permit has no equals())
    private static Permit permitMatching(User user, Zone zone, int priceCents) {
        return argThat(permit -> permit.getUser() == user
                && permit.getZone() == zone
                && permit.getPricePaidCents() == priceCents
                && TODAY.equals(permit.getPurchaseDate()));
    }

    // a user who passes checks 1, 3 and 4: exists, no permit, no citations
    private void stubUserWithNoPermitAndNoCitations(User user) {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(permitRepository.findByUserId(USER_ID)).thenReturn(List.of());
        when(citationRepository.findByUserId(USER_ID)).thenReturn(List.of());
    }

    /* ---------- R1 - Zones and Prices ---------- */

    @ParameterizedTest(name = "R1 - {0} buying {1} should be charged {2} cents and get that permit")
    @CsvSource({
            "STUDENT,  GREEN, 6000",
            "STUDENT,  BLUE,  18000",
            "EMPLOYEE, GREEN, 9000",
            "EMPLOYEE, RED,   24000"
    })
    void R1_allowedZoneShouldChargeFixedPriceForType(UserType type, Zone zone, int priceCents) {
        // Setup: an eligible user, an approved charge, and the permit the repository will hand back
        User user = new User("Bob", type, "BOB-123");
        Permit saved = new Permit(user, zone, priceCents, TODAY);
        stubUserWithNoPermitAndNoCitations(user);
        when(dateProvider.today()).thenReturn(TODAY);
        when(paymentGateway.charge(USER_ID, priceCents)).thenReturn(true);
        when(permitRepository.save(permitMatching(user, zone, priceCents))).thenReturn(saved);

        // Act
        Permit result = permitService.purchasePermit(USER_ID, zone);

        // Assert: the saved permit comes back, and the card was charged that price before it was saved
        assertEquals(saved, result);
        verify(paymentGateway).charge(USER_ID, priceCents);
        verify(permitRepository).save(permitMatching(user, zone, priceCents));
    }

    @ParameterizedTest(name = "R1 - {0} buying {1} should be refused and charge nothing")
    @CsvSource({
            "STUDENT,  RED",
            "EMPLOYEE, BLUE"
    })
    void R1_disallowedZoneShouldBeRefused(UserType type, Zone zone) {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(new User("Bob", type, "BOB-123")));

        assertThrows(PermitNotAllowedException.class, () -> permitService.purchasePermit(USER_ID, zone));

        verifyNoInteractions(permitRepository, citationRepository, paymentGateway, notificationService);
    }

    /* ---------- R2 - One Permit ---------- */

    @Test
    @DisplayName("R2 - A user who already holds a permit should be refused, charged nothing, and sent nothing")
    void R2_userWithExistingPermitShouldBeRefused() {
        User user = new User("Bob", UserType.STUDENT, "BOB-123");
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(permitRepository.findByUserId(USER_ID)).thenReturn(List.of(permitFor(Zone.GREEN, 6000)));

        assertThrows(PermitNotAllowedException.class, () -> permitService.purchasePermit(USER_ID, Zone.BLUE));

        verify(permitRepository, never()).save(permitMatching(user, Zone.BLUE, 18000));
        verifyNoInteractions(paymentGateway, notificationService);
    }

    /* ---------- R3 - Unpaid Citations Block a Purchase ---------- */

    @Test
    @DisplayName("R3 - A user with an unpaid citation should be refused before being charged")
    void R3_userWithUnpaidCitationShouldBeRefused() {
        User user = new User("Bob", UserType.STUDENT, "BOB-123");
        Citation paid = new Citation(user, TODAY.minusDays(30), 2500);
        paid.setPaid(true);
        Citation unpaid = new Citation(user, TODAY.minusDays(2), 2500);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(permitRepository.findByUserId(USER_ID)).thenReturn(List.of());
        when(citationRepository.findByUserId(USER_ID)).thenReturn(List.of(paid, unpaid));

        assertThrows(PermitNotAllowedException.class, () -> permitService.purchasePermit(USER_ID, Zone.GREEN));

        verify(permitRepository, never()).save(permitMatching(user, Zone.GREEN, 6000));
        verifyNoInteractions(paymentGateway, notificationService);
    }

    @Test
    @DisplayName("R3 - A user whose citations are all paid should be able to buy a permit")
    void R3_userWithOnlyPaidCitationsShouldBeAllowed() {
        User user = new User("Bob", UserType.STUDENT, "BOB-123");
        Citation paid = new Citation(user, TODAY.minusDays(30), 2500);
        paid.setPaid(true);
        Permit saved = new Permit(user, Zone.GREEN, 6000, TODAY);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(permitRepository.findByUserId(USER_ID)).thenReturn(List.of());
        when(citationRepository.findByUserId(USER_ID)).thenReturn(List.of(paid));
        when(dateProvider.today()).thenReturn(TODAY);
        when(paymentGateway.charge(USER_ID, 6000)).thenReturn(true);
        when(permitRepository.save(permitMatching(user, Zone.GREEN, 6000))).thenReturn(saved);

        Permit result = permitService.purchasePermit(USER_ID, Zone.GREEN);

        assertEquals(saved, result);
        verify(paymentGateway).charge(USER_ID, 6000);
        verify(permitRepository).save(permitMatching(user, Zone.GREEN, 6000));
    }

    /* ---------- R4 - Order of Checks ---------- */

    @Test
    @DisplayName("R4 - A user that doesn't exist should be refused before anything else is checked")
    void R4_missingUserShouldBeCheckedFirst() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> permitService.purchasePermit(USER_ID, Zone.GREEN));

        verifyNoInteractions(permitRepository, citationRepository, paymentGateway, notificationService);
    }

    @Test
    @DisplayName("R4 - A student with an unpaid citation asking for RED should fail on the zone, never looking at citations")
    void R4_zoneShouldBeCheckedBeforeCitations() {
        // both exceptions are PermitNotAllowedException, so the untouched repositories are what prove the order
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(new User("Bob", UserType.STUDENT, "BOB-123")));

        assertThrows(PermitNotAllowedException.class, () -> permitService.purchasePermit(USER_ID, Zone.RED));

        verifyNoInteractions(permitRepository, citationRepository, paymentGateway, notificationService);
    }

    @Test
    @DisplayName("R4 - A user with a permit and an unpaid citation should fail on the permit, never looking at citations")
    void R4_existingPermitShouldBeCheckedBeforeCitations() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(new User("Bob", UserType.STUDENT, "BOB-123")));
        when(permitRepository.findByUserId(USER_ID)).thenReturn(List.of(permitFor(Zone.GREEN, 6000)));

        assertThrows(PermitNotAllowedException.class, () -> permitService.purchasePermit(USER_ID, Zone.BLUE));

        verifyNoInteractions(citationRepository, paymentGateway, notificationService);
    }

    /* ---------- R5 - Payment and Confirmation ---------- */

    @Test
    @DisplayName("R5 - An approved charge should save the permit, send a confirmation with it, and return it")
    void R5_approvedChargeShouldSaveSendAndReturnPermit() {
        User user = new User("Bob", UserType.STUDENT, "BOB-123");
        Permit saved = new Permit(user, Zone.BLUE, 18000, TODAY);
        stubUserWithNoPermitAndNoCitations(user);
        when(dateProvider.today()).thenReturn(TODAY);
        when(paymentGateway.charge(USER_ID, 18000)).thenReturn(true);
        when(permitRepository.save(permitMatching(user, Zone.BLUE, 18000))).thenReturn(saved);

        Permit result = permitService.purchasePermit(USER_ID, Zone.BLUE);

        assertEquals(saved, result);
        verify(paymentGateway).charge(USER_ID, 18000);
        verify(permitRepository).save(permitMatching(user, Zone.BLUE, 18000));
        verify(notificationService).sendPurchaseConfirmation(user, saved);
    }

    @Test
    @DisplayName("R5 - A declined charge should throw and save no permit and send no confirmation")
    void R5_declinedChargeShouldNotSaveOrSend() {
        User user = new User("Bob", UserType.STUDENT, "BOB-123");
        stubUserWithNoPermitAndNoCitations(user);
        when(paymentGateway.charge(USER_ID, 6000)).thenReturn(false);

        assertThrows(PaymentDeclinedException.class, () -> permitService.purchasePermit(USER_ID, Zone.GREEN));

        verify(paymentGateway).charge(USER_ID, 6000);
        verify(permitRepository, never()).save(permitMatching(user, Zone.GREEN, 6000));
        verifyNoInteractions(notificationService);
    }

    /* ---------- R11 - Finding User's Permit ---------- */

    @Test
    @DisplayName("R11 - Trying to get a user's permit when they have none should return empty")
    void R11_getUserPermitForUserWithNoPermitShouldReturnEmpty() {
        when(permitRepository.findByUserId(USER_ID)).thenReturn(List.of());

        assertTrue(permitService.findPermitForUser(USER_ID).isEmpty());
    }

    @Test
    @DisplayName("R11 - Trying to get a user's permit when they have one should return that one permit")
    void R11_getUserPermitForUserWithOnePermitShouldReturnOnePermit() {
        Permit permit = permitFor(Zone.GREEN, 6000);
        when(permitRepository.findByUserId(USER_ID)).thenReturn(List.of(permit));

        assertEquals(Optional.of(permit), permitService.findPermitForUser(USER_ID));
    }

    @Test
    @DisplayName("R11 - Trying to get a user's permit when they have two should return the first permit")
    void R11_getUserPermitForUserWithTwoPermitsShouldReturnFirstPermit() {
        Permit first = permitFor(Zone.GREEN, 6000);
        Permit second = permitFor(Zone.BLUE, 18000);
        when(permitRepository.findByUserId(USER_ID)).thenReturn(List.of(first, second));

        // Optional.equals compares the permit with equals(), which is identity here,
        // so this also proves it's the first one and not the second one
        assertEquals(Optional.of(first), permitService.findPermitForUser(USER_ID));
    }
}
