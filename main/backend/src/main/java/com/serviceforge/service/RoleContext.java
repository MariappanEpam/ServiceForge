package com.serviceforge.service;

import org.springframework.stereotype.Component;

@Component
public class RoleContext {

    public enum Role {
        admin,
        dispatcher,
        technician
    }

    public Role parseRole(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return Role.admin;
        }
        try {
            return Role.valueOf(headerValue.trim());
        } catch (IllegalArgumentException ex) {
            return Role.admin;
        }
    }

    public void requireAdminOrDispatcher(Role role) {
        if (role != Role.admin && role != Role.dispatcher) {
            throw new IllegalStateException("Forbidden: requires admin or dispatcher role");
        }
    }
}
