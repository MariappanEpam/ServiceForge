package com.serviceforge.exception;

/** ETSTCC-13074: feedback already exists for the job (maps to 409 FEEDBACK_ALREADY_EXISTS). */
public class DuplicateFeedbackException extends RuntimeException {

    public DuplicateFeedbackException(long jobId) {
        super("Feedback already exists for job: " + jobId);
    }
}
