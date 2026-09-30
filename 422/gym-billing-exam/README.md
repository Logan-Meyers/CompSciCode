# Midterm 1 Makeup Programming Section: Gym Membership Billing

Name: Logan Meyers

## Part A: One test case per rule

The decision table has six rules, R1 to R6. Write one test case for each rule: the inputs you will use and the result the table says to expect. Row R1 is filled in as an example.

| Rule | memberAge | months | plan | Expected result |
| --- | --- | --- | --- | --- |
| R1 | 15 | 6 | BASIC | 120 |
| R2 | 15 | 6 | PLUS | 240 |
| R3 | 15 | 6 | ELITE | throws IllegalArgumentException |
| R4 | 56 | 6 | BASIC | 180 |
| R5 | 56 | 6 | PLUS | 300 |
| R6 | 56 | 6 | ELITE | 480 |

## Part B: Robust boundary values

For each input, list exactly seven values: min-, min, min+, nom, max-, max, and max+ of its valid range. Use the valid range as given in the specification. Do not split a range by age group or plan.

Nominal values used while another input is varied: memberAge 56 (nom), months 6 (nom), plan BASIC

| ID | Input | Type (min-, min, min+, nom, max-, max, max+) | Value |
| --- | --- | --- | --- |
| B1 | months | min- | 0 |
| B2 | months | min | 1 |
| B3 | months | min+ | 2 |
| B4 | months | nom | 6 |
| B5 | months | max- | 11 |
| B6 | months | max | 12 |
| B7 | months | max+ | 13 |
| B8 | memberAge | min- | 12 |
| B9 | memberAge | min | 13 |
| B10 | memberAge | min+ | 14 |
| B11 | memberAge | nom | 56 |
| B12 | memberAge | max- | 98 |
| B13 | memberAge | max | 99 |
| B14 | memberAge | max+ | 100 |

## Part D: Failures found

One row per failing test.

| Test method | Inputs (age, months, plan) | Expected | Actual | Design IDs |
| --- | --- | --- | --- | --- |
| test_R2 | 15, 6, PLUS | 240 | 300 | R2 |
| test_R3 | 15, 6, ELITE | throws IllegalArgumentException | returns 480 (no exception thrown) | R3 |
| test_B6_monthsMax | 56, 12, BASIC | 360 | throws IllegalArgumentException | B6 |
| test_B8_memberAgeMinMinus | 12, 6, BASIC | throws IllegalArgumentException | returns 120 (no exception thrown) | B8 |
| test_B13_memberAgeMax | 99, 6, BASIC | 180 | throws IllegalArgumentException | B13 |
