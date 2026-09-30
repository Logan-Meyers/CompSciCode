# AI Chat Log

## My Prompt:

Hey Junie. I need you to update the Testing file with more tests. On line 37 and below, create tests for strong normal equivalence classes, and robust boundary value tests. Do not update the existing tests.

Here is some info about equivalence classes:
- driver age can be 21 to 99
- days can be 1 to 30
- carClass can be ECONOMY, STANDARD, or PREMIUM

For pricing:
- ECONOMY is 40, STANDARD is 60, and PREMIUM is 100 for daily pricing
- drivers aged 21-24 are charged an extra 15$ a day
- drivers aged 21-24 cannot rent PREMIUM cars
- Total = days × (daily rate + surcharge).

quote throws IllegalArgumentException when any input is outside its valid values (including a null carClass), or when a driver aged 21 to 24 requests PREMIUM

DO NOT try to decompile the class. Only use the info I've given and what's in the testing file. You do not need to touch CHATLOG.md. You may update the README.md with appropriate testing numbers that have been implemented as tests for parts A and B. I will do part D. Do not run the program, just write the code.

## Response:

1. Update README.md with Part A equivalence classes and Part B robust boundary value specifications.
2. Add Strong Normal Equivalence Class tests (E1_E2_E5, E1_E2_E6, E4_E2_E3, E4_E2_E5, E4_E2_E6) to RentalQuoteTest.java.
3. Add Robust Boundary Value tests (B3 through B18) to RentalQuoteTest.java.
4. Verify code formatting and submit solution.

I will now update the test file with the strong normal equivalence class tests and robust boundary value tests, and update the README table.

The README file for the car rental quote project was opened, revealing sections on equivalence classes and a table for recording test failures.

The README file was updated to include a complete table of valid equivalence classes for each input with continued IDs and added rows.

The README file was updated to include detailed robust boundary value test entries for driverAge and days with corresponding IDs and values.

Multiple unit tests covering various driver ages, rental days, and car classes were added to verify correct pricing and exception handling in the rental quote calculation.

I am now reviewing the implemented tests and documentation before submitting.

### Summary
- Added strong normal equivalence class tests and robust boundary value tests to `RentalQuoteTest.java`.
- Updated `README.md` with equivalence classes (Part A) and robust boundary values (Part B).

### Changes
- Updated `README.md`:
    - Completed Part A table with valid equivalence classes `E4` (driverAge 21 to 24), `E5` (carClass STANDARD), and `E6` (carClass PREMIUM).
    - Completed Part B table with nominal values and robust BVT entries `B3` through `B18` covering boundary values for `days` (1 to 30), `driverAge` (21 to 24), and `driverAge` (25 to 99).
- Updated `src/test/java/edu/wsu/cpts422/rental/RentalQuoteTest.java`:
    - Added strong normal ECT test methods: `test_E1_E2_E5`, `test_E1_E2_E6`, `test_E4_E2_E3`, `test_E4_E2_E5`, and `test_E4_E2_E6`.
    - Added robust BVT test methods: `test_B3_daysMinPlus` through `test_B18_driverAge25to99MaxPlus`.
    - Preserved existing tests unchanged.

### Verification
- Verified test structure, naming conventions, and expected pricing formulas against specifications without running the program or decompiling the binaries.

## My Prompt: 

There are 6 failing tests, which is expected. Please use the following info to update the README's part D table with failing tests
- test B7 fails because no exception is thrown
- Test B14 fails because the extra $15 is thrown on per day when it shouldn't be
- test E4 E2 E5 failes because it gets 210 when it should be 225; no extra charge is added for young driver
- test E4 E2 E6 fails because it doesn't throw an exception for a young driver trying to rent a premium car
- test B5 fails because of invalid input, when it should be valid still
- test B12 fails because an extra 15$ was added on each day

## Response:

I am about to search the project chat log for information related to Part D.

The README file was updated to include a detailed table listing the failing tests with their inputs, expected and actual results, and design IDs.

### Summary
- Updated the Part D table in `README.md` with entries for all 6 failing tests.

### Changes
- Modified `README.md` Part D table to record the failing test methods (`test_E4_E2_E5`, `test_E4_E2_E6`, `test_B5_daysMax`, `test_B7_driverAge21to24MinMinus`, `test_B12_driverAge21to24MaxPlus`, and `test_B14_driverAge25to99Min`), their test inputs, expected behavior, actual observed outcomes, and corresponding design IDs.

### Verification
- Verified all input values, expected outputs, actual outputs, and design IDs against `RentalQuoteTest.java` and problem specifications.