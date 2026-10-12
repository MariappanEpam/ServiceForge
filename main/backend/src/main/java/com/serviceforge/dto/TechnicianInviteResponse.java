package com.serviceforge.dto;

public class TechnicianInviteResponse {

    private TechnicianResponse technician;
    private String invitationToken;

    public TechnicianInviteResponse(TechnicianResponse technician, String invitationToken) {
        this.technician = technician;
        this.invitationToken = invitationToken;
    }

    public TechnicianResponse getTechnician() {
        return technician;
    }

    public String getInvitationToken() {
        return invitationToken;
    }
}
