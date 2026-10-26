package com.serviceforge.dto;

import com.serviceforge.model.Feedback;

/** ETSTCC-13074: 201 body for POST /api/feedback. Contains no customer or technician names. No toString(). */
public class FeedbackResponse {

    private Long id;
    private Long jobId;
    private Integer rating;
    private String comment;
    private String createdAt;

    public static FeedbackResponse from(Feedback feedback) {
        FeedbackResponse r = new FeedbackResponse();
        r.id = feedback.getId();
        r.jobId = feedback.getJobId();
        r.rating = feedback.getRating();
        r.comment = feedback.getComment();
        r.createdAt = feedback.getCreatedAt().toString();
        return r;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
