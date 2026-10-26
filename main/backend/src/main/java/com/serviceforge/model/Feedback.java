package com.serviceforge.model;

import java.time.Instant;

/**
 * ETSTCC-13074: immutable stored feedback for one completed job.
 * Personal-data note: {@code comment} is free text and may contain personal data. Consent is enforced
 * upstream (FeedbackRequest requires consent=true) and is not stored. Intentionally has no toString().
 */
public final class Feedback {

    private final Long id;
    private final Long jobId;
    private final int rating;
    private final String comment;
    private final Instant createdAt;

    public Feedback(Long id, Long jobId, int rating, String comment, Instant createdAt) {
        this.id = id;
        this.jobId = jobId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getJobId() {
        return jobId;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
