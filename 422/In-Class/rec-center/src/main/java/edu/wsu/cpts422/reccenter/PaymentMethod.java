package edu.wsu.cpts422.reccenter;

public enum PaymentMethod {
    CASH("Cash"),
    CARD("Card");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
