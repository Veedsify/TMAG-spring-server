package com.TravelMedicineAdvisory.Server.domain.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AcceptInvitationRequest {
    private String token;

    @JsonProperty("new_password")
    private String newPassword;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
