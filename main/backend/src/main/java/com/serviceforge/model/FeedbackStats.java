package com.serviceforge.model;

/** ETSTCC-13074: aggregate (count, rating sum) of feedback for one technician. No toString(). */
public final class FeedbackStats {

    private final long count;
    private final long ratingSum;

    public FeedbackStats(long count, long ratingSum) {
        this.count = count;
        this.ratingSum = ratingSum;
    }

    public long getCount() {
        return count;
    }

    public long getRatingSum() {
        return ratingSum;
    }
}
