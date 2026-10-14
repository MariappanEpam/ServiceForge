package com.serviceforge.migrated.api;

public class BadRequestException extends RuntimeException {
    private final String code;

    public BadRequestException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
