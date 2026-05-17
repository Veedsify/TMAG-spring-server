package com.TravelMedicineAdvisory.Server.domain.auth;

import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    private String planCode;

    @JsonProperty("affiliate_referral_code")
    private String affiliateReferralCode;

    @JsonProperty("account_type")
    private String accountType;

    @JsonProperty("billing_currency")
    private BillingCurrency billingCurrency;

    public RegisterRequest() {
    }

    public RegisterRequest(String first_name, String last_name, String username, String email,
            String password, String planCode, BillingCurrency billingCurrency) {
        this.firstName = first_name;
        this.lastName = last_name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.planCode = planCode;
        this.billingCurrency = billingCurrency;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public BillingCurrency getBillingCurrency() {
        return billingCurrency;
    }

    public void setBillingCurrency(BillingCurrency billingCurrency) {
        this.billingCurrency = billingCurrency;
    }
}
