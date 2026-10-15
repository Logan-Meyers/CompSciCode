package edu.wsu.cpts422.reccenter;

public enum Plan {
    MONTHLY("Monthly", 4_000),
    QUARTERLY("Quarterly", 11_000),
    ANNUAL("Annual", 40_000);

    private final String label;
    private final long baseFeeCents;

    Plan(String label, long baseFeeCents) {
        this.label = label;
        this.baseFeeCents = baseFeeCents;
    }

    public String getLabel() {
        return label;
    }

    public long getBaseFeeCents() {
        return baseFeeCents;
    }
}
