package com.serviceforge.service;

import com.serviceforge.data.MockDataStore;
import com.serviceforge.dto.ActivateTechnicianRequest;
import com.serviceforge.model.Technician;
import com.serviceforge.model.TechnicianStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class TechnicianOnboardingService {

    private final MockDataStore mockDataStore;

    private final SecureRandom secureRandom = new SecureRandom();

    public TechnicianOnboardingService(MockDataStore mockDataStore) {
        this.mockDataStore = mockDataStore;
    }

    public String generateAndIndexToken(Long technicianId) {
        String token = generateToken();
        mockDataStore.indexInvitationToken(token, technicianId);
        return token;
    }

    public Technician activate(ActivateTechnicianRequest req) {
        if (req == null || req.getToken() == null || req.getToken().isBlank()) {
            throw new IllegalArgumentException("Invalid invitation token");
        }

        // Normalize token (defensive against accidental whitespace/newlines)
        req.setToken(req.getToken().trim());

        // Locate technician by token.
        // Prefer the token index (one-time token semantics), fall back to scanning for backward-compat.
        Technician t = mockDataStore.findTechnicianByInvitationToken(req.getToken())
            .orElseGet(() -> mockDataStore.getAllTechnicians().stream()
                .filter(x -> req.getToken().equals(x.getInvitationToken()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token")));

        // Backward-compat: if token exists on technician but index is missing, restore it.
        if (t.getInvitationToken() != null && !t.getInvitationToken().isBlank()) {
            mockDataStore.indexInvitationToken(t.getInvitationToken(), t.getId());
        }

        if (t.getStatus() != TechnicianStatus.INVITED) {
            throw new IllegalStateException("Invalid technician status transition: " + t.getStatus() + " -> ACTIVE");
        }

        t.setStatus(TechnicianStatus.ACTIVE);
        t.setPreferredWorkingHours(req.getPreferredWorkingHours());
        t.setEmergencyContact(req.getEmergencyContact());
        t.setOnboardingCompletedAt(LocalDateTime.now());

        // One-time token: remove index and clear stored token
        mockDataStore.removeInvitationToken(req.getToken());
        t.setInvitationToken(null);

        return t;
    }

    private String generateToken() {
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
