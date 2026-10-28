package org.cpts422.services;

import org.cpts422.exceptions.BookNotFoundException;
import org.cpts422.exceptions.RentalNotAllowedException;
import org.cpts422.exceptions.UserNotFoundException;
import org.cpts422.models.Book;
import org.cpts422.models.BookCategory;
import org.cpts422.models.MembershipType;
import org.cpts422.models.User;
import org.cpts422.repositories.BookRepository;
import org.cpts422.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 1, 1);
    private static final String TITLE = "Effective Java";

    @Mock private BookRepository bookRepository;
    @Mock private UserRepository userRepository;
    @Mock private PaymentService paymentService;
    @Mock private DateProvider dateProvider;

    @InjectMocks private RentalService rentalService;

    /* ---------- Rental Tests ---------- */

    @ParameterizedTest(name = "{0} renting {1} book is due in {2} days")
    @DisplayName("rentBook returns the correct due date for each membership and category")
    @CsvSource({
            "STUDENT,   REGULAR,     21",
            "STUDENT,   NEW_RELEASE,  7",
            "COMMUNITY, REGULAR,     21",
            "COMMUNITY, NEW_RELEASE,  7",
            "FACULTY,   REGULAR,     42",
            "FACULTY,   NEW_RELEASE, 14"
    })
    void rentBookAvailableBookReturnsExpectedDueDate(
            MembershipType membership, BookCategory category, int expectedDays) {
        // Arrange
        User user = new User("Alice", 1, membership);
        Book book = new Book(TITLE, "Joshua Bloch", category);
        when(userRepository.findUserById(1)).thenReturn(user);
        when(bookRepository.findBookByTitle(TITLE)).thenReturn(book);
        when(paymentService.getOutstandingBalance(user)).thenReturn(0);
        when(bookRepository.countBooksRentedBy(user)).thenReturn(0);
        when(dateProvider.today()).thenReturn(TODAY);

        // Act
        LocalDate dueDate = rentalService.rentBook(1, TITLE);

        // Assert: returned value
        assertEquals(TODAY.plusDays(expectedDays), dueDate);

        // Assert: the book saved back is rented to this user with the same due date
        ArgumentCaptor<Book> saved = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).updateBook(saved.capture());
        assertFalse(saved.getValue().available());
        assertEquals(user, saved.getValue().currentUser());
        assertEquals(dueDate, saved.getValue().dueDate());

        // Verify: the book has been added to the user's list of books
        verify(bookRepository).addBookToUser(user, saved.getValue());
    }

    @Test
    @DisplayName("A non-existent user trying to rent a book.")
    void nonExistentUserRentsBookThrowsUserNotFound() {
        // have user repo return null (no user) when finding user by id 1
        when(userRepository.findUserById(1)).thenReturn(null);

        // Assert: the user does not exist when trying to rent a book
        assertThrows(UserNotFoundException.class, () -> rentalService.rentBook(1, TITLE));

        // Verify: the book has not been rented
        verify(bookRepository, never()).updateBook(any());
        verify(bookRepository, never()).removeBookFromUser(any(), any());
    }

    @Test
    @DisplayName("A user trying to rent a book which doesn't exist")
    void rentNonExistentBookThrowsBookNotFound() {
        // Arrange/setup
        User user = new User("Alice", 1, MembershipType.STUDENT);
        when(userRepository.findUserById(1)).thenReturn(user);
        when(bookRepository.findBookByTitle("Void Book")).thenReturn(null);

        // Assert: trying to rent the book errors
        assertThrows(BookNotFoundException.class, () -> rentalService.rentBook(1, "Void Book"));

        // Verify: no books at all have been rented or updated
        verify(bookRepository, never()).updateBook(any());
        verify(bookRepository, never()).addBookToUser(any(), any());
    }

    @Test
    @DisplayName("A user trying to rent a reference book")
    void rentReferenceBookThrowsRentalNotAllowed() {
        // Arrange/setup
        User user = new User("Alice", 1, MembershipType.STUDENT);
        Book book = new Book(TITLE, "Joshua Bloch", BookCategory.REFERENCE);
        when(userRepository.findUserById(1)).thenReturn(user);
        when(bookRepository.findBookByTitle(TITLE)).thenReturn(book);

        // Assert: trying to rent the book errors
        assertThrows(RentalNotAllowedException.class, () -> rentalService.rentBook(1, TITLE));

        // Verify: no books at all have been rented or updated
        verify(bookRepository, never()).updateBook(any());
        verify(bookRepository, never()).addBookToUser(any(), any());
    }

    @Test
    @DisplayName("A user trying to rent a book which has already been rented")
    void rentAlreadyRentedBookThrowsRentalNotAllowed() {
        // Setup
        User borrower = new User("John", 2, MembershipType.STUDENT);
        Book rentedBook = new Book(TITLE, "Joshua Bloch", BookCategory.REGULAR)
                .rentTo(new User("Alice", 1, MembershipType.STUDENT), TODAY.plusDays(21));
        when(userRepository.findUserById(2)).thenReturn(borrower);
        when(bookRepository.findBookByTitle(TITLE)).thenReturn(rentedBook);

        // Assert: make sure borrower cannot rent book
        assertThrows(RentalNotAllowedException.class, () -> rentalService.rentBook(2, TITLE));

        // Verify: no changes have been made to the book repository
        verify(bookRepository, never()).updateBook(any());
        verify(bookRepository, never()).addBookToUser(any(), any());
    }

    @Test
    @DisplayName("A user trying to rent a book with an outstanding balance")
    void rentWithOutstandingBalanceThrowsRentalNotAllowed() {
        // Setup
        User user1 = new User("Tim", 1, MembershipType.STUDENT);
        Book book = new Book(TITLE, "Joshua Bloch", BookCategory.REGULAR);
        when(userRepository.findUserById(1)).thenReturn(user1);
        when(paymentService.getOutstandingBalance(user1)).thenReturn(1000);  // owes $10
        when(bookRepository.findBookByTitle(TITLE)).thenReturn(book);

        // Assert: user tring to rent should error since they have a balance
        assertThrows(RentalNotAllowedException.class, () -> rentalService.rentBook(1, TITLE));

        // Verify: no changes have been made to the book repository
        verify(bookRepository, never()).updateBook(any());
        verify(bookRepository, never()).addBookToUser(any(), any());
    }

    @ParameterizedTest(name = "{0} renting book at rental limit of {1} throws RentalNotAllowedException")
    @DisplayName("A user trying to rent a book at rental limit should throw")
    @CsvSource({
            "STUDENT,   REGULAR,     5",
            "FACULTY,   REGULAR,     10",
            "COMMUNITY, REGULAR,     2",
    })
    void rentAtRentalLimitThrowsRentalNotAllowed(
            MembershipType membership, BookCategory category, int rentalLimit
    ) {
        // Setup
        User user1 = new User("Tim", 1, membership);
        Book book = new Book(TITLE, "Joshua Bloch", BookCategory.REGULAR);
        when(userRepository.findUserById(1)).thenReturn(user1);
        when(bookRepository.countBooksRentedBy(user1)).thenReturn(rentalLimit);
        when(bookRepository.findBookByTitle(TITLE)).thenReturn(book);

        // Assert: user tring to rent should error since they have a balance
        assertThrows(RentalNotAllowedException.class, () -> rentalService.rentBook(1, TITLE));

        // Verify: no changes have been made to the book repository
        verify(bookRepository, never()).updateBook(any());
        verify(bookRepository, never()).addBookToUser(any(), any());
    }

    /* ---------- Returning Tests ---------- */

    @Test
    @DisplayName("A non-existent user trying to return a book.")
    void nonExistentUserReturnsBookThrowsUserNotFound() {
        // have user repo return null (no user) when finding user by id 1
        when(userRepository.findUserById(1)).thenReturn(null);

        // Assert: the user does not exist when trying to rent a book
        assertThrows(UserNotFoundException.class, () -> rentalService.returnBook(1, TITLE));

        // Assert: the book has not been rented
        verify(bookRepository, never()).updateBook(any());
        verify(bookRepository, never()).removeBookFromUser(any(), any());
    }


    @Test
    @DisplayName("A user tries to return a non-existent book.")
    void returnsNonExistentHookThrowsBookNotFound() {
        // Setup
        User user = new User("Alice", 1, MembershipType.STUDENT);
        when(userRepository.findUserById(1)).thenReturn(user);
        when(bookRepository.findBookByTitle("Void Book")).thenReturn(null);

        // Assert: should throw error since book doesn't exist
        assertThrows(BookNotFoundException.class, () -> rentalService.returnBook(1, "Void Book"));

        // Validate: returned value
        verify(bookRepository, never()).updateBook(any());
        verify(bookRepository, never()).removeBookFromUser(any(), any());
    }

    @Test
    @DisplayName("A user tries to return a book that is not rented by anyone")
    void returnUnrentedBookThrowsBookNotFound() {
        // Setup
        User user = new User("Alice", 1, MembershipType.STUDENT);
        Book book = new Book(TITLE, "Joshua Bloch", BookCategory.REGULAR);
        when(userRepository.findUserById(1)).thenReturn(user);
        when(bookRepository.findBookByTitle(TITLE)).thenReturn(book);

        // Assert: should throw error since nobody has the book
        assertThrows(BookNotFoundException.class, () -> rentalService.returnBook(1, TITLE));

        // Verify: nothing was charged or changed
        verify(paymentService, never()).chargeLateFee(any(), anyInt());
        verify(bookRepository, never()).updateBook(any());
        verify(bookRepository, never()).removeBookFromUser(any(), any());
    }

    @Test
    @DisplayName("A user tries to return a book rented by a different user")
    void returnBookRentedByOtherUserThrowsBookNotFound() {
        // Setup
        User user = new User("Alice", 1, MembershipType.STUDENT);
        User otherUser = new User("John", 2, MembershipType.STUDENT);
        Book book = new Book(TITLE, "Joshua Bloch", BookCategory.REGULAR)
                .rentTo(otherUser, TODAY.plusDays(21));
        when(userRepository.findUserById(1)).thenReturn(user);
        when(bookRepository.findBookByTitle(TITLE)).thenReturn(book);

        // Assert: should throw error since the book belongs to someone else
        assertThrows(BookNotFoundException.class, () -> rentalService.returnBook(1, TITLE));

        // Verify: nothing was charged or changed
        verify(paymentService, never()).chargeLateFee(any(), anyInt());
        verify(bookRepository, never()).updateBook(any());
        verify(bookRepository, never()).removeBookFromUser(any(), any());
    }

    @Test
    @DisplayName("A user returns a book on time and is not charged")
    void returnBookOnTimeChargesNoFee() {
        // Setup
        User user = new User("Alice", 1, MembershipType.COMMUNITY);
        Book book = new Book(TITLE, "Joshua Bloch", BookCategory.REGULAR)
                .rentTo(user, TODAY);
        when(userRepository.findUserById(1)).thenReturn(user);
        when(bookRepository.findBookByTitle(TITLE)).thenReturn(book);
        when(dateProvider.today()).thenReturn(TODAY);

        // Act
        int fee = rentalService.returnBook(1, TITLE);

        // Assert: no fee returned and none charged
        assertEquals(0, fee);
        verify(paymentService, never()).chargeLateFee(any(), anyInt());

        // Verify: the book was returned to the shelf and removed from the user
        ArgumentCaptor<Book> saved = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).updateBook(saved.capture());
        assertTrue(saved.getValue().available());
        assertNull(saved.getValue().currentUser());
        verify(bookRepository).removeBookFromUser(user, saved.getValue());
    }

    @Test
    @DisplayName("A user returns a late book and is charged the late fee")
    void returnBookLateChargesFee() {
        // Setup: community member, regular book, 3 days late => 3 * 50 cents
        User user = new User("Alice", 1, MembershipType.COMMUNITY);
        Book book = new Book(TITLE, "Joshua Bloch", BookCategory.REGULAR)
                .rentTo(user, TODAY.minusDays(3));
        when(userRepository.findUserById(1)).thenReturn(user);
        when(bookRepository.findBookByTitle(TITLE)).thenReturn(book);
        when(dateProvider.today()).thenReturn(TODAY);

        // Act
        int fee = rentalService.returnBook(1, TITLE);

        // Assert: fee returned and charged
        assertEquals(150, fee);
        verify(paymentService).chargeLateFee(user, 150);

        // Verify: the book was returned to the shelf and removed from the user
        ArgumentCaptor<Book> saved = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).updateBook(saved.capture());
        assertTrue(saved.getValue().available());
        verify(bookRepository).removeBookFromUser(user, saved.getValue());
    }

    /* ---------- Calculate Late Fee Tests ---------- */

    @ParameterizedTest(name = "{0} returning {1} book (rented={2}) {3} days after due costs {4} cents")
    @DisplayName("calculateLateFee returns the correct fee for each membership, category and lateness")
    @CsvSource({
            // book never rented (no due date)
            "STUDENT,   REGULAR,      false, 0,   0",
            // returned on or before the due date
            "COMMUNITY, REGULAR,      true,  -1,  0",
            "COMMUNITY, REGULAR,      true,  0,   0",
            // within grace period (community 0, student 2, faculty 7)
            "STUDENT,   REGULAR,      true,  2,   0",
            "FACULTY,   REGULAR,      true,  7,   0",
            // regular books, first chargeable day
            "COMMUNITY, REGULAR,      true,  1,   50",
            "STUDENT,   REGULAR,      true,  3,   25",
            "FACULTY,   REGULAR,      true,  8,   10",
            // regular books, several chargeable days
            "COMMUNITY, REGULAR,      true,  4,   200",
            "STUDENT,   REGULAR,      true,  6,   100",
            "FACULTY,   REGULAR,      true,  12,  50",
            // new releases, first chargeable day
            "COMMUNITY, NEW_RELEASE,  true,  1,   100",
            "STUDENT,   NEW_RELEASE,  true,  3,   50",
            "FACULTY,   NEW_RELEASE,  true,  8,   25",
            // new releases, within grace period
            "STUDENT,   NEW_RELEASE,  true,  2,   0",
            "FACULTY,   NEW_RELEASE,  true,  7,   0",
            // fee exactly at the cap is not reduced
            "COMMUNITY, NEW_RELEASE,  true,  20,  2000",
            // fee over the cap is limited to the cap
            "COMMUNITY, NEW_RELEASE,  true,  21,  2000",
            "FACULTY,   REGULAR,      true,  365, 2000"
    })
    void calculateLateFeeReturnsExpectedFee(
            MembershipType membership, BookCategory category, boolean rented, int daysLate, int expectedFee) {
        // Arrange
        User user = new User("Alice", 1, membership);
        LocalDate dueDate = TODAY;
        Book book = new Book(TITLE, "Joshua Bloch", category);
        if (rented) {
            book = book.rentTo(user, dueDate);
        }

        // Act
        int fee = rentalService.calculateLateFee(book, user, dueDate.plusDays(daysLate));

        // Assert
        assertEquals(expectedFee, fee);
    }
}
