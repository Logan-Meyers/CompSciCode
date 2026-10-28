# Chat Log

## Turn 1

**User:** Hey, Claude! I am learning Mockito in a software testing class. You're given access to the Java project to use as a reference for code content and structure, do not edit any code unless I say otherwise. The only file I would like you to write to is a `chatlog.md` of our entire conversation, so keep that updated always.

We're going to need to write full unit testing for the project in Junit5 and mockito with 100% coverage and eventually we'll need to refactor to make code clean and simple.

But for now, let's start with this: RentalService takes four dependencies in its constructor: BookRepository, UserRepository, PaymentService, and DateProvider. Before we write any tests, explain what @Mock and @InjectMocks will do here, and show me an empty test class skeleton with no test methods yet.

**Claude:**

Read `RentalService.java` and `pom.xml` (JUnit 5.13.4, Mockito 5.20.0 with `mockito-junit-jupiter`, JaCoCo, Java 17). All four dependencies are interfaces, and `src/test/java` exists but has no tests yet.

### What `@Mock` does
Each `@Mock` field becomes a fake implementation of that interface, created by Mockito. By default every method returns a "empty" value (`null` for objects, `0` for ints, `false` for booleans), and every call is recorded so you can `verify(...)` it later. You control behavior with `when(mock.method(...)).thenReturn(...)`. Here that gives four mocks: `BookRepository`, `UserRepository`, `PaymentService`, `DateProvider`.

### What `@InjectMocks` does
`@InjectMocks` builds the real object under test (`RentalService`) and passes the mocks into it. Mockito picks the biggest constructor and matches parameters to mocks by type, so it effectively runs `new RentalService(bookRepository, userRepository, paymentService, dateProvider)`. The service is real; only its collaborators are fake.

### What makes the annotations work
Annotations alone do nothing. `@ExtendWith(MockitoExtension.class)` (from `mockito-junit-jupiter`) tells JUnit 5 to create the mocks and inject them before each test. Each test gets fresh mocks, so no state leaks between tests. The extension also enables strict stubs: an unused `when(...)` stub fails the test.

### Skeleton (not written to disk; shown in chat only)

```java
package org.cpts422.services;

import org.cpts422.repositories.BookRepository;
import org.cpts422.repositories.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private DateProvider dateProvider;

    @InjectMocks
    private RentalService rentalService;
}
```

## Turn 2

**User:** Shared the testing requirements (`@Mock`/`@InjectMocks`, stubbing with `when/thenReturn`, `verify()` including `never()`, no `LocalDate.now()`, a separate `@Spy` class on a real `InMemoryPaymentService` for the late-return-then-blocked-rental scenario, parameterized tests, `@DisplayName` plus camelCase names, and the list of scenarios). Proposed starting by converting the big-bang test in `Main` into separate unit tests: add a book, add a user, rent, compare the returned date; non-existent user tries rent/return; missing, reference, and already-rented books (user 1 rents, then user 2 tries). Asked if anything is wrong with this thinking.

**Claude:** Read `Main.java`, `Book`, `User`, `BookRepository`, `PaymentService`, `InMemoryPaymentService`, `DateProvider`. Feedback given:

1. **Main point:** the plan is written as an integration test ("add a book", "add a user", "user 1 rents, then user 2 tries"). With mocks there is no state, so nothing is "added" and nothing is remembered between calls. Each test instead stubs what the repositories return for that one call. The "user 2 rents a rented book" case becomes a single test: stub `findBookByTitle` to return a book with `available == false`.
2. One test per branch, each checking one outcome, instead of a scenario that walks through several steps.
3. Only the `@Spy` class (real `InMemoryPaymentService`) needs real state across two calls: return late (fee charged), then rent (blocked).
4. `Main` only reaches a few branches (student and faculty, regular and new release, one reference rejection, one on-time return). Late fees, balance block, rental limit, `UserNotFound`, `BookNotFound`, and "already rented" are not covered, so branch coverage needs more than a port of `Main`.
5. Stub only what the code reaches (strict stubs fail on unused stubs). Guard clauses throw before later calls, so use `verify(..., never())` on `updateBook`/`addBookToUser`/`chargeLateFee` for the error cases.
6. Check the due date with parameterized tests over membership and category: REGULAR 21 days, NEW_RELEASE 7, FACULTY doubles.
7. Reminder: `returnBook` with a null book and with a book rented by someone else are two different branches of the same `if`, so test both.

## Turn 3

**User:** Asked to draft a `rentBook` happy-path test to see how it looks with mocking.

**Claude:** Checked `MembershipType` (STUDENT, FACULTY, COMMUNITY) and `BookCategory` (REGULAR, NEW_RELEASE, REFERENCE). Drafted, in chat only, a `@ParameterizedTest` with `@CsvSource` over membership and category (6 rows: 21/7 days, faculty doubled to 42/14). Fixed date `2025-01-01` stubbed on `DateProvider`. Stubs: user, book, zero balance, zero books rented. Asserts the returned due date, then `verify`s `updateBook` with an `ArgumentCaptor` (book unavailable, held by the user, same due date) and `addBookToUser`. Noted `@InjectMocks` should work with the constructor, and that the stubs are all used, so strict stubs pass.

## Turn 4

**User:** Said they understand mocking without state, added the happy-path test (date changed to 2026) and several tests of their own (coverage 42% -> 54%). Asked for feedback on style, setup, and execution, and what is missing. Stuck on `rentAlreadyRentedBook`: should it be simplified to "book isn't available", or keep the user1/user2 flow by capturing the rented book and re-stubbing the repo mock?

**Claude:** Read `RentalServiceTest.java` and `SystemDateProviderTest.java` (reviewed by reading only; tests were not run). Feedback given:

- **Stuck test:** `book` is an immutable record, so `rentalService.rentBook(1, ...)` never changes it and `assertFalse(book.available())` fails. `when(book.available())` can't work either: records are final and not mocks. Recommended simplifying: stub `findBookByTitle` to return `new Book(...).rentTo(user1, date)` so `available()` is false, expect `RentalNotAllowedException`, and verify `updateBook`/`addBookToUser` never called. Only one user is needed, and `dateProvider` and the second user are not needed. The capture-and-re-stub approach does work (`thenReturn(saved.getValue())`) but re-tests the happy path and mostly tests the mock wiring.
- **`nonExistentUserRentsOrReturnsBook`:** name and `@DisplayName` promise rent and return, but only rent is tested. Split into two tests; use `verifyNoInteractions(bookRepository)` since the user check comes first.
- **Style:** names should say method, condition, and result (for example `rentBookReferenceBookThrowsRentalNotAllowed`); `times(1)` is the default so it's redundant; a stray commented-out `when(book.available())` and trailing `@DisplayName` should go; helper factory methods for `User`/`Book` instead of `@BeforeEach` stubs (strict stubs would fail unused ones).
- **`SystemDateProviderTest`:** `@ExtendWith(MockitoExtension.class)` is unneeded (no mocks); `LocalDate.now()` in the assertion can fail across midnight. Suggested capturing before/after dates and asserting the result is in range.
- **Missing for branch coverage:** unpaid balance (0 vs positive), rental limit for each membership at limit-1 and limit (2 / 5 / 10), everything in `returnBook` and `calculateLateFee`, plus `InMemory*` repositories, `InMemoryPaymentService`, and `Main`, which count toward total coverage.

## Turn 5

**User:** Running all tests with coverage in IntelliJ using JaCoCo, the coverage tree now lists the test classes (e.g. `RentalServiceTest`). Asked if that's wrong and how to stop it.

**Claude:** Checked `pom.xml`: the Maven JaCoCo plugin only reports on `target/classes`, so it never includes tests. The behavior comes from IntelliJ's coverage settings: Run | Edit Configurations | (the test run configuration) | Code Coverage tab, uncheck "Enable coverage in test folders" (and check the include/exclude class patterns); a newly created run configuration (e.g. a fresh "All in package/directory" run) can have it on. Nothing in the code or `pom.xml` needs changing. Test classes should not appear and their presence inflates the coverage percentage. The Maven report (`target/site/jacoco/index.html`) is an easy cross-check.

## Turn 6

**User:** Said they implemented the rest of the `RentalService` tests and have 100% branch coverage of it. Next is the `@Spy` test; asked for an explanation of how it works and an unrelated example to base their test and understanding on.

**Claude:** Explained `@Spy`: wraps a real object, so real methods run by default while calls are still recorded and individually stubbable. `@Mock` replaces behavior, `@Spy` keeps it. Gave an unrelated example (`CheckoutService` with `@Mock PriceCatalog`, `@Mock AuditLog`, `@Spy LoyaltyLedger`, `@InjectMocks`): the first purchase adds real points to the ledger, the second purchase is discounted because the real state persisted; `verify(ledger).addPoints(...)` plus `assertEquals` on the real state. Notes: `@InjectMocks` also injects `@Spy` fields by type (constructor injection), spy must be a concrete class, use `doReturn(...).when(spy)` to stub (since `when(spy.method())` runs the real method), and the test must live in a separate class from the one that mocks `PaymentService`. Mapped the example to their `InMemoryPaymentService` scenario (late return, charge, then blocked rental).

## Turn 7

**User:** Wrote the spy test in a separate class `RentalServicePaymentSpyTest`, in the same style as the other tests.

**Claude:** Read `RentalServicePaymentSpyTest.java` (reviewed by reading only; not run). Verdict: meets every requirement (`@Mock` x3, `@Spy InMemoryPaymentService`, `@InjectMocks`, fixed date, `verify` on the spy, real balance asserted, late-return-then-blocked-rental). Fee math checks out (faculty, regular, 12 days late, 7 grace, 10 cents/day = 50); all stubs are used, so strict stubs pass. Suggestions: (1) `assertThrows` alone can't tell which of the four `RentalNotAllowedException` branches fired, so assert the message ("Alice has unpaid late fees"); (2) `times(2)` on `getOutstandingBalance` counts the test's own assertion call, which is brittle, use `atLeastOnce()` or drop it; (3) also verify `removeBookFromUser` and that `chargeLateFee` was called exactly once; (4) consider a contrast spy test (on-time return, `never()` charge, balance 0, second rental succeeds) to show the block is caused by the fee; (5) redundant `any` import; `TODAY` constant is duplicated across test classes (a refactor candidate).

## Turn 8

**User:** Applied some of the spy-test suggestions (message assertion, `atLeastOnce()`), then began the refactor: `rentBook` (complexity 11) and `calculateLateFee` (complexity 12) were split using three helpers; all methods now at complexity 9 or below.

**Claude:** Read the refactored `RentalService.java` (reviewed by reading only; tests not re-run). Behavior looks preserved (same checks, order, and messages). My own count: `rentBook` 9, `calculateLateFee` 7, `dailyRateCents` 6, matching their claim. Feedback: (1) `findUserOrThrow`/`findBookOrThrow` are `static` and take the repository as a parameter; simpler as private instance methods using the fields (`dailyRateCents` can stay static, it is pure); (2) `findBookOrThrow` is passed an eagerly built message, even on success; could be built only on failure; (3) `rentBook` still has two inline membership chains (`rentalLimit`, `loanDays`) and `calculateLateFee` still has `graceDays`, so extract `rentalLimitFor`, `loanDaysFor`, `graceDaysFor`; (4) the same FACULTY/STUDENT/else chain appears four times, a candidate for enum fields on `MembershipType` or switch expressions; (5) magic numbers as named constants; `System.out.println` in the service as a later cleanup; (6) the long comment block explains the metric, not the code, and could be trimmed; (7) rerun all tests with coverage to confirm refactor safety; unrelated files (`In-Class/...`, `Screenshots/`, `htmlReport/`) show in `git status`, so stage only intended files.

## Turn 9

**User:** Decided to leave the code as it is for this homework, not worrying about the smaller optimizations. Thanked Claude for the help.

**Claude:** Agreed that's reasonable. Reminded to rerun the full suite with coverage and to stage only the homework files before submitting. Chat log is up to date.
