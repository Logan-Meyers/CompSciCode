package org.cpts422.services;

import org.cpts422.exceptions.RentalNotAllowedException;
import org.cpts422.models.Book;
import org.cpts422.models.BookCategory;
import org.cpts422.models.MembershipType;
import org.cpts422.models.User;
import org.cpts422.repositories.BookRepository;
import org.cpts422.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServicePaymentSpyTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 1, 1);
    private static final String LATE_TITLE = "Effective Java";
    private static final String SECOND_TITLE = "Clean Code";

    @Mock private BookRepository bookRepository;
    @Mock private UserRepository userRepository;
    @Mock private DateProvider dateProvider;

    // Real payment service wrapped in a spy so its balance map persists between calls
    @Spy private InMemoryPaymentService paymentService = new InMemoryPaymentService();

    @InjectMocks private RentalService rentalService;

    @Test
    @DisplayName("A late fee from a returned book blocks the same user from renting another book")
    void lateReturnFeeBlocksSubsequentRental() {
        // Setup: faculty member returns a regular book 12 days late => (12 - 7 grace) * 10 = 50 cents
        User user = new User("Alice", 1, MembershipType.FACULTY);
        Book lateBook = new Book(LATE_TITLE, "Joshua Bloch", BookCategory.REGULAR)
                .rentTo(user, TODAY.minusDays(12));
        Book secondBook = new Book(SECOND_TITLE, "Robert Martin", BookCategory.REGULAR);
        when(userRepository.findUserById(1)).thenReturn(user);
        when(bookRepository.findBookByTitle(LATE_TITLE)).thenReturn(lateBook);
        when(bookRepository.findBookByTitle(SECOND_TITLE)).thenReturn(secondBook);
        when(dateProvider.today()).thenReturn(TODAY);

        // Act: return the late book
        int fee = rentalService.returnBook(1, LATE_TITLE);

        // Assert: the fee was charged through the spy and is stored as a real balance
        assertEquals(50, fee);
        verify(paymentService).chargeLateFee(user, 50);
        assertEquals(50, paymentService.getOutstandingBalance(user));

        // Act/Assert: the real balance now blocks the next rental
        RentalNotAllowedException ex = assertThrows(RentalNotAllowedException.class,
                () -> rentalService.rentBook(1, SECOND_TITLE));
        assertEquals("Alice has unpaid late fees", ex.getMessage());

        // Verify: the balance was checked by the rental (once above in the assert, once by the service),
        // and the only book update was the earlier return
        verify(paymentService, atLeastOnce()).getOutstandingBalance(user);
        verify(bookRepository, times(1)).updateBook(any());
        verify(bookRepository, never()).addBookToUser(any(), any());
    }
}
