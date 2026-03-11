package com.TravelMedicineAdvisory.Server.domain.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResetPasswordRequest {

    private String email;
    private String token;
    @JsonProperty(value = "new_password")
    private String newPassword;

    public ResetPasswordRequest() {}

    public ResetPasswordRequest(String email, String token, String newPassword) {
        this.email = email;
        this.token = token;
        this.newPassword = newPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
