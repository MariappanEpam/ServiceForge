package com.serviceforge.exception;

/**
 * ETSTCC-13074: storing or reading feedback failed (falls through to the existing 500 INTERNAL_ERROR handler).
 * The message is fixed text only; the cause is never echoed in responses.
 */
public class FeedbackPersistenceException extends RuntimeException {

    public FeedbackPersistenceException(String fixedMessage, Throwable cause) {
        super(fixedMessage, cause);
    }
}
