package com.serviceforge.controller;

import com.serviceforge.dto.ApiError;
import com.serviceforge.exception.DuplicateFeedbackException;
import com.serviceforge.exception.InvalidFeedbackException;
import com.serviceforge.exception.JobNotCompletedException;
import com.serviceforge.exception.JobNotFoundException;
import com.serviceforge.exception.TechnicianNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;

/**
 * ETSTCC-13074: exception mapping scoped to FeedbackController only (existing handlers and contracts are
 * unchanged). Messages are fixed text; parser/validator exception messages are never echoed because they
 * can contain the submitted comment. FeedbackPersistenceException is intentionally not handled here and
 * falls through to the existing catch-all (500 INTERNAL_ERROR).
 */
@ControllerAdvice(assignableTypes = FeedbackController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FeedbackExceptionHandler {

    private String resolveRequestId(WebRequest request) {
        String rid = request.getHeader("X-Request-Id");
        return (rid == null || rid.isEmpty()) ? UUID.randomUUID().toString() : rid;
    }

    private ResponseEntity<ApiError> respond(HttpStatus status, String code, String message, WebRequest request) {
        ApiError error = new ApiError(status.value(), code, message, message, resolveRequestId(request));
        return new ResponseEntity<>(error, new HttpHeaders(), status);
    }

    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<ApiError> handleJobNotFound(JobNotFoundException ex, WebRequest request) {
        return respond(HttpStatus.NOT_FOUND, "JOB_NOT_FOUND", "Job not found", request);
    }

    @ExceptionHandler(TechnicianNotFoundException.class)
    public ResponseEntity<ApiError> handleTechnicianNotFound(TechnicianNotFoundException ex, WebRequest request) {
        return respond(HttpStatus.NOT_FOUND, "TECHNICIAN_NOT_FOUND", "Technician not found", request);
    }

    @ExceptionHandler(JobNotCompletedException.class)
    public ResponseEntity<ApiError> handleNotCompleted(JobNotCompletedException ex, WebRequest request) {
        return respond(HttpStatus.BAD_REQUEST, "JOB_NOT_COMPLETED", "Feedback can only be given for completed jobs", request);
    }

    @ExceptionHandler(DuplicateFeedbackException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateFeedbackException ex, WebRequest request) {
        return respond(HttpStatus.CONFLICT, "FEEDBACK_ALREADY_EXISTS", "Feedback already submitted for this job", request);
    }

    @ExceptionHandler(InvalidFeedbackException.class)
    public ResponseEntity<ApiError> handleInvalid(InvalidFeedbackException ex, WebRequest request) {
        return respond(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getField() + ": " + ex.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex, WebRequest request) {
        return respond(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Request body is missing or malformed", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        return respond(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Invalid identifier in request path", request);
    }
}
