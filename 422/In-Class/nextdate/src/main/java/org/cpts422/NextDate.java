package org.cpts422;

/**
 * NextDate: given a date, returns the date of the following day.
 * Valid input: month 1 to 12, day 1 to the last day of that month, year 1812 to 2212.
 * Output format: month/day/year, for example 3/1/2024.
 */
public class NextDate {

    public String nextDate(int month, int day, int year) {
        if (year < 1812 || year >= 2212) {
            throw new IllegalArgumentException("Invalid year: " + year);
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid month: " + month);
        }
        int lastDay = daysInMonth(month, year);
        if (day < 1 || day > lastDay) {
            throw new IllegalArgumentException("Invalid day: " + day);
        }

        if (day < lastDay) {
            return month + "/" + (day + 1) + "/" + year;
        }
        if (month < 12) {
            return (month + 1) + "/1/" + year;
        }
        return "1/1/" + (year + 1);
    }

    static int daysInMonth(int month, int year) {
        if (month == 1 || month == 3 || month == 5 || month == 7
                || month == 8 || month == 10 || month == 12) {
            return 31;
        } else if (month == 4 || month == 6 || month == 9 || month == 11) {
            return 30;
        } else {
            // February
            if (year % 400 == 0) {
                return 29;
            }
            if (year % 100 == 0) {
                return 28;
            }
            if (year % 4 == 0) {
                return 29;
            }
            return 28;
        }
    }
}
