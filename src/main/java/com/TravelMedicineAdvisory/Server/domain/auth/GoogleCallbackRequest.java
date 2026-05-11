package com.TravelMedicineAdvisory.Server.domain.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GoogleCallbackRequest {
    private String code;
    private String planCode;

    @JsonProperty("affiliate_referral_code")
    private String affiliateReferralCode;

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

    public String getAffiliateReferralCode() {
        return affiliateReferralCode;
    }

    public void setAffiliateReferralCode(String affiliateReferralCode) {
        this.affiliateReferralCode = affiliateReferralCode;
    }
}
