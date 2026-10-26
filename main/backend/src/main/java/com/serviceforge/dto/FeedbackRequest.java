package com.serviceforge.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * ETSTCC-13074: POST /api/feedback body.
 * Personal-data note: {@code comment} may contain personal data; its length is checked in FeedbackService
 * (not with @Size) so a rejected value is never embedded in a validation message. Consent is validated
 * here and never stored. No toString() on purpose (the framework logs request bodies at DEBUG).
 */
public class FeedbackRequest {

    @NotNull
    @Positive
    @JsonDeserialize(using = StrictIntegerDeserializer.class)
    private Long jobId;

    @NotNull
    @Min(1)
    @Max(5)
    @JsonDeserialize(using = StrictIntegerDeserializer.class)
    private Integer rating;

    private String comment;

    @NotNull
    @AssertTrue(message = "consent must be given")
    private Boolean consent;

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getConsent() {
        return consent;
    }

    public void setConsent(Boolean consent) {
        this.consent = consent;
    }
}
