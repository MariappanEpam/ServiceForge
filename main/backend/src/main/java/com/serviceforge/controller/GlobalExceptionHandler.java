package com.serviceforge.controller;

import com.serviceforge.dto.ApiError;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

    private String resolveRequestId(WebRequest request) {
        String rid = request.getHeader("X-Request-Id");
        return (rid == null || rid.isEmpty()) ? UUID.randomUUID().toString() : rid;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex, WebRequest request) {
        String requestId = resolveRequestId(request);
        ApiError error = new ApiError(HttpStatus.CONFLICT.value(), "CONFLICT", ex.getMessage(), ex.getMessage(), requestId);
        return new ResponseEntity<>(error, new HttpHeaders(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, WebRequest request) {
        String requestId = resolveRequestId(request);
        ApiError error = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_ERROR", "Internal server error", "An unexpected error occurred", requestId);
        return new ResponseEntity<>(error, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        String requestId = resolveRequestId(request);
        String msg = ex.getBindingResult().getFieldErrors().stream().map(f -> f.getField() + ": " + f.getDefaultMessage()).findFirst().orElse("Validation failed");
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR", msg, msg, requestId);
        return new ResponseEntity<>(error, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
}
