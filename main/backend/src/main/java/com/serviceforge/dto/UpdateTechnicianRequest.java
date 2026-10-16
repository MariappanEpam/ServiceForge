package com.serviceforge.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateTechnicianRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String trade;

    @NotBlank
    private String region;

    private String email;
    private String phone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTrade() {
        return trade;
    }

    public void setTrade(String trade) {
        this.trade = trade;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
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
}
