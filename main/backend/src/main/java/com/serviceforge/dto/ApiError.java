package com.serviceforge.dto;

import java.time.Instant;

public class ApiError {

    private final String code;         // machine code like INSUFFICIENT_STOCK
    private final String message;      // developer-facing message
    private final String userMessage;  // user-facing message
    private final String timestamp;    // ISO-8601
    private final String requestId;    // optional request id for tracing
    private final int status;          // HTTP status

    public ApiError(int status, String code, String message, String userMessage, String requestId) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.userMessage = userMessage;
        this.requestId = requestId;
        this.timestamp = Instant.now().toString();
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getRequestId() {
        return requestId;
    }

    public int getStatus() {
        return status;
    }
}
