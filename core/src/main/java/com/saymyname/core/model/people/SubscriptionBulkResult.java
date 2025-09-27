package com.saymyname.core.model.people;

public class SubscriptionBulkResult {
    private final long matched;
    private final long acted;
    private final long skipped;
    private final double seconds;

    public SubscriptionBulkResult(long matched, long acted, long skipped, double seconds) {
        this.matched = matched;
        this.acted = acted;
        this.skipped = skipped;
        this.seconds = seconds;
    }

    public long getMatched() {
        return matched;
    }

    public long getActed() {
        return acted;
    }

    public long getSkipped() {
        return skipped;
    }

    public double getSeconds() {
        return seconds;
    }
}
