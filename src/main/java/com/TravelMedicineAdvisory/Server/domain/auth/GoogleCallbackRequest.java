package com.TravelMedicineAdvisory.Server.domain.auth;

public class GoogleCallbackRequest {
    private String code;
    private String planCode;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }
}
