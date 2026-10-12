package com.serviceforge.model;

public class Technician {

    private final Long id;
    private String name;
    private String region;

    // Feature 3 — Technician onboarding and management
    private TechnicianStatus status;
    private String trade;
    private String email;
    private String phone;
    private String preferredWorkingHours;
    private String emergencyContact;
    private java.time.LocalDateTime onboardingCompletedAt;
    /**
     * Server-side only. Must never be returned from GET list/detail endpoints.
     * Returned only on create/reinvite responses.
     */
    private String invitationToken;

    public Technician(Long id, String name, String region) {
        this.id = id;
        this.name = name;
        this.region = region;

        // Backwards-compatible defaults for seeded technicians
        this.status = TechnicianStatus.ACTIVE;
    }

    public Technician(Long id,
                      String name,
                      String region,
                      TechnicianStatus status,
                      String trade,
                      String email,
                      String phone) {
        this.id = id;
        this.name = name;
        this.region = region;
        this.status = status;
        this.trade = trade;
        this.email = email;
        this.phone = phone;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public TechnicianStatus getStatus() {
        return status;
    }

    public void setStatus(TechnicianStatus status) {
        this.status = status;
    }

    public String getTrade() {
        return trade;
    }

    public void setTrade(String trade) {
        this.trade = trade;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPreferredWorkingHours() {
        return preferredWorkingHours;
    }

    public void setPreferredWorkingHours(String preferredWorkingHours) {
        this.preferredWorkingHours = preferredWorkingHours;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public java.time.LocalDateTime getOnboardingCompletedAt() {
        return onboardingCompletedAt;
    }

    public void setOnboardingCompletedAt(java.time.LocalDateTime onboardingCompletedAt) {
        this.onboardingCompletedAt = onboardingCompletedAt;
    }

    public String getInvitationToken() {
        return invitationToken;
    }

    public void setInvitationToken(String invitationToken) {
        this.invitationToken = invitationToken;
    }
}
