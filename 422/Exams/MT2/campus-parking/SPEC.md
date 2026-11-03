# Campus Parking Permits: Specification

## Overview

Campus Parking Permits is a Spring MVC web application where students and employees buy parking permits and receive citations for parking violations. This specification defines the business rules of the service layer (`UserService`, `PermitService`, and `CitationService`). Each rule has an ID (R1, R2, ...) and, where useful, a worked example. The rules state the required behavior: what the code must do, which is not necessarily what it does now.

All money is stored as whole cents in an `int`. For example, $25.00 is stored as 2500.

## Domain Model

- **User**: an ID, a name, a type, one vehicle license plate, and one payment card. The type is either `STUDENT` or `EMPLOYEE`. The plate and the card are entered once, when the user registers.
- **Zone**: a group of campus lots shown as a color on the parking map: `GREEN` (remote lots), `BLUE` (residence halls), or `RED` (central campus).
- **Permit**: belongs to one user and one zone, and covers the user's registered vehicle. It records the price paid and the purchase date.
- **Citation**: belongs to one user. It records the date it was issued, the fine, and whether it has been paid.

**Note:** the application has no registration flow; the users are preloaded. Choosing a user's name on the Home page is the same as logging in as that user, and choosing the Officer link is the same as logging in as a parking officer. Since there is no profile page, the Home page is expected to list each user's license plate too, so you can copy a plate and use it on the Officer page to issue a citation.

## Permit Rules

**R1. Zones and prices.** A permit must be sold only for a zone allowed for the user's type, at the fixed price for that type and zone.

| Zone | STUDENT (cents) | EMPLOYEE (cents) |
| --- | --- | --- |
| GREEN | 6000 | 9000 |
| BLUE | 18000 | not allowed |
| RED | not allowed | 24000 |

*Example:* an employee buying a RED permit should pay 24000 cents ($240.00). A student asking for a RED permit should be refused.

**R2. One permit.** A user must not hold more than one permit. Permits do not expire.

**R3. Unpaid citations block a purchase.** A user with any unpaid citation must not be able to buy a permit.

**R4. Order of checks.** `purchasePermit(userId, zone)` must check, in this order:

1. The user exists.
2. The zone is allowed for the user's type (R1).
3. The user does not already hold a permit (R2).
4. The user has no unpaid citations (R3).

The first failed check must decide the exception, and no later check may run. A refused purchase must charge nothing, save nothing, and send nothing.

*Example:* a student with an unpaid citation asks for a RED permit. Check 2 fails first, so the purchase should be refused because of the zone (R1). The citations should never be looked at, and nothing should be charged, saved, or sent.

**R5. Payment and confirmation.** The price must be charged through the payment gateway only after all four checks in R4 pass. In particular, the card must never be charged before confirming that the user has no unpaid citations.

- If the charge is approved, the permit must be saved with the price and today's date as its purchase date, a purchase confirmation with the saved permit must be sent to the user, and the saved permit must be returned.
- If the charge is declined, no permit may be saved and no confirmation may be sent.

## Citation Rules

There is one type of violation. Fines are expected to rise in fixed steps when violations repeat.

A parking officer's app reads the license plate of the parked vehicle. It is expected to look up the user registered to that plate and then ask the citation service to issue a citation to that user.

**R6. Fine steps.** The fine steps are 2500, 5000, and 10000 cents.

**R7. Repeat violations.** `issueCitation(userId)` must look at the user's most recent previous citation. Both paid and unpaid citations must count toward increasing fines.

- If it was issued 15 days or fewer before the new citation, counting calendar days between the two dates, the new fine must be one step above the previous fine, up to the last step.
- Otherwise, or if the user has no previous citation, the new fine must be the first step.

*Example:* one student's citations, in order.

| Citation date | Days since previous citation | Fine (cents) | Why |
| --- | --- | --- | --- |
| 2026-10-01 | none | 2500 | First citation, so the first step |
| 2026-10-16 | 15 | 5000 | 15 days or fewer, so one step up |
| 2026-10-20 | 4 | 10000 | One step up again |
| 2026-10-25 | 5 | 10000 | Already at the last step |
| 2026-11-10 | 16 | 2500 | More than 15 days, so back to the first step |

**R8. Citation notice.** Every new citation must be saved with today's date as its issue date, the fine from R6 and R7, and marked unpaid. A citation notice with the saved citation must be sent to the user, and the saved citation must be returned.

**R9. Paying a citation.** `payCitation(citationId)` must charge the fine through the payment gateway, to the card of the user the citation belongs to.

- An unknown citation, or one already paid, must be refused, and nothing may be charged.
- If the charge is approved, the citation must be marked paid, saved, and returned. A citation must never be marked paid before the charge is approved.
- If the charge is declined, the citation must stay unpaid.

## Lookup Rules

The pages are expected to read users, permits, and citations only through these service operations.

**R10. Finding users.** `getAllUsers()` must return every user. `getUser(userId)` must return the user with that ID, and `findByLicensePlate(plate)` the user registered to that plate; each of the two must throw `UserNotFoundException` when no user matches.

**R11. A user's permit.** `findPermitForUser(userId)` must return the user's permit, or an empty result when the user has none.

**R12. A user's citations.** `findCitationsForUser(userId)` must return all of the user's citations, both paid and unpaid.

## Collaborators

The services are expected to receive all of their dependencies through their constructors.

| Collaborator | Used by | Used for |
| --- | --- | --- |
| `UserRepository` | `UserService`, `PermitService`, `CitationService` | Looking up users |
| `PermitRepository` | `PermitService` | Finding a user's permits, saving new permits |
| `CitationRepository` | `PermitService`, `CitationService` | Finding a user's citations, saving citations |
| `PaymentGateway` | `PermitService`, `CitationService` | Charging the payment card the user saved at registration; it is expected to return whether the charge was approved. Card details must never pass through the services. |
| `NotificationService` | `PermitService`, `CitationService` | Sending purchase confirmations and citation notices |
| `DateProvider` | `PermitService`, `CitationService` | Supplying today's date; services must never call `LocalDate.now()` directly |

## Errors

Exception messages are not part of the specification: tests should check the exception type, not the message text.

| Exception | Must be thrown when |
| --- | --- |
| `UserNotFoundException` | `getUser`, `findByLicensePlate`, `purchasePermit`, or `issueCitation` finds no matching user (R4, R10) |
| `PermitNotAllowedException` | A zone is not allowed for the type (R1), the user already holds a permit (R2), or the user has an unpaid citation (R3) |
| `CitationNotFoundException` | `payCitation` gets an unknown citation ID |
| `CitationAlreadyPaidException` | `payCitation` gets a citation that is already paid |
| `PaymentDeclinedException` | The payment gateway declines a charge (R5, R9) |

## Web Interface

The interface is deliberately minimal. Four sample users are expected to be preloaded when the application starts: two students and two employees, one of whom starts with an unpaid citation and another with a permit. There is no registration, login, or profile editing.

1. **Home**: is expected to list the users. The users are hardcoded with all the information the User model needs, so choosing one is the same as logging in as that user and should open that user's page.
2. **User page**: is expected to show the user's permit, if any, and their citations. A form should buy a permit for a chosen zone, and each unpaid citation should have a Pay button.
3. **Officer page**: a form is expected to take a license plate, look up the user registered to it, and issue a citation to that user.

Every action is expected to show a success or error message on the same page.