package com.serviceforge.dto;

import java.math.BigDecimal;

/** ETSTCC-13074: technician rating summary. Never carries comments, customer names or other personal data. */
public class FeedbackSummaryResponse {

    private Long technicianId;
    private BigDecimal averageRating; // scale 1, or null when there is no feedback
    private Long feedbackCount;

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }

    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    public Long getFeedbackCount() {
        return feedbackCount;
    }

    public void setFeedbackCount(Long feedbackCount) {
        this.feedbackCount = feedbackCount;
    }
}
