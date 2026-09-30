# Midterm 1 Programming Section: Car Rental Quote

Name: Logan Meyers

## Part A: Strong normal equivalence classes

List every valid equivalence class for each input. Strong normal ECT uses valid classes only. Continue the IDs (E4, E5, ...) and add rows as needed.

| ID | Input | Valid class |
| --- | --- | --- |
| E1 | driverAge | 25 to 99 |
| E2 | days | 1 to 30 |
| E3 | carClass | ECONOMY |
| E4 | driverAge | 21 to 24 |
| E5 | carClass | STANDARD |
| E6 | carClass | PREMIUM |

## Part B: Robust boundary values

List the robust BVT values for `driverAge` and `days`. For each valid range, list min-, min, min+, max-, max, and max+. Continue the IDs (B3, B4, ...) and add rows as needed.

Nominal values used while another input is varied: driverAge 40, carClass ECONOMY, days 3

| ID | Input | Range | Type | Value |
| --- | --- | --- | --- | --- |
| B1 | days | 1 to 30 | min- | 0 |
| B2 | days | 1 to 30 | min | 1 |
| B3 | days | 1 to 30 | min+ | 2 |
| B4 | days | 1 to 30 | max- | 29 |
| B5 | days | 1 to 30 | max | 30 |
| B6 | days | 1 to 30 | max+ | 31 |
| B7 | driverAge | 21 to 24 | min- | 20 |
| B8 | driverAge | 21 to 24 | min | 21 |
| B9 | driverAge | 21 to 24 | min+ | 22 |
| B10 | driverAge | 21 to 24 | max- | 23 |
| B11 | driverAge | 21 to 24 | max | 24 |
| B12 | driverAge | 21 to 24 | max+ | 25 |
| B13 | driverAge | 25 to 99 | min- | 24 |
| B14 | driverAge | 25 to 99 | min | 25 |
| B15 | driverAge | 25 to 99 | min+ | 26 |
| B16 | driverAge | 25 to 99 | max- | 98 |
| B17 | driverAge | 25 to 99 | max | 99 |
| B18 | driverAge | 25 to 99 | max+ | 100 |

## Part D: Failures found

One row per failing test.

| Test method | Inputs (age, days, class) | Expected | Actual | Design IDs |
| --- | --- | --- | --- | --- |
| test_E4_E2_E5 | (22, 3, STANDARD) | 225 | 210 | E4, E2, E5 |
| test_E4_E2_E6 | (22, 3, PREMIUM) | IllegalArgumentException | No exception thrown | E4, E2, E6 |
| test_B5_daysMax | (40, 30, ECONOMY) | 1200 | IllegalArgumentException | B5 |
| test_B7_driverAge21to24MinMinus | (20, 3, ECONOMY) | IllegalArgumentException | No exception thrown | B7 |
| test_B12_driverAge21to24MaxPlus | (25, 3, ECONOMY) | 120 | 165 | B12 |
| test_B14_driverAge25to99Min | (25, 3, ECONOMY) | 120 | 165 | B14 |
