package com.serviceforge.dto;

import jakarta.validation.constraints.NotBlank;

public class ActivateTechnicianRequest {

    @NotBlank
    private String token;

    private String preferredWorkingHours;
    private String emergencyContact;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
}
