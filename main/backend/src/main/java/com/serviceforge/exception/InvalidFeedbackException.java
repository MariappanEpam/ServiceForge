package com.serviceforge.exception;

/**
 * ETSTCC-13074: feedback input rejected (maps to 400 VALIDATION_ERROR).
 * Deliberately does not accept or store the rejected value (it may be personal data).
 */
public class InvalidFeedbackException extends RuntimeException {

    private final String field;

    public InvalidFeedbackException(String field, String fixedMessage) {
        super(fixedMessage);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
