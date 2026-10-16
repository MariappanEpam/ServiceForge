package com.serviceforge.dto;

import com.serviceforge.model.Technician;
import com.serviceforge.model.TechnicianStatus;

import java.time.LocalDateTime;

public class TechnicianResponse {

    private Long id;
    private String name;
    private String region;
    private TechnicianStatus status;
    private String trade;
    private String email;
    private String phone;
    private String preferredWorkingHours;
    private String emergencyContact;
    private LocalDateTime onboardingCompletedAt;

    public static TechnicianResponse from(Technician t) {
        TechnicianResponse r = new TechnicianResponse();
        r.id = t.getId();
        r.name = t.getName();
        r.region = t.getRegion();
        r.status = t.getStatus();
        r.trade = t.getTrade();
        r.email = t.getEmail();
        r.phone = t.getPhone();
        r.preferredWorkingHours = t.getPreferredWorkingHours();
        r.emergencyContact = t.getEmergencyContact();
        r.onboardingCompletedAt = t.getOnboardingCompletedAt();
        return r;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRegion() {
        return region;
    }

    public TechnicianStatus getStatus() {
        return status;
    }

    public String getTrade() {
        return trade;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPreferredWorkingHours() {
        return preferredWorkingHours;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public LocalDateTime getOnboardingCompletedAt() {
        return onboardingCompletedAt;
    }
}
