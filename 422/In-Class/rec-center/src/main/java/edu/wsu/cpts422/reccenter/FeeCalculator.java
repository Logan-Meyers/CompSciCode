package edu.wsu.cpts422.reccenter;

import org.springframework.stereotype.Component;

/** Pure fee math. No database, no network. All money is in cents. */
@Component
public class FeeCalculator {

    static final int MAX_DISCOUNT_PERCENT = 35;
    static final int CARD_SURCHARGE_PERCENT = 3;

    public int discountPercent(int age, String state) {
        int percent = 0;
        if (age < 18) {
            percent += 25;          // youth
        } else if (age >= 65) {
            percent += 30;          // senior
        }
        if ("WA".equals(state)) {
            percent += 10;
        } else if ("ID".equals(state)) {
            percent += 5;
        }
        return Math.min(percent, MAX_DISCOUNT_PERCENT);
    }

    public long discountCents(long baseFeeCents, int discountPercent) {
        return baseFeeCents * discountPercent / 100;
    }

    /** 3% of the discounted amount, rounded half up to the nearest cent. */
    public long cardSurchargeCents(long discountedCents) {
        return (discountedCents * CARD_SURCHARGE_PERCENT + 50) / 100;
    }
}
