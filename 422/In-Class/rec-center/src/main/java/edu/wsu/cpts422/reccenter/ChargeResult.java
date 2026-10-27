package edu.wsu.cpts422.reccenter;

/** What the payment gateway says about a charge. */
public record ChargeResult(boolean approved, String transactionId, String message) {

    public static ChargeResult approved(String transactionId) {
        return new ChargeResult(true, transactionId, "Approved");
    }

    public static ChargeResult declined(String message) {
        return new ChargeResult(false, null, message);
    }
}
