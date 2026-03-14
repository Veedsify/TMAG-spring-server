package com.TravelMedicineAdvisory.Server.domain.auth;

import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResponse {
    private Long id;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String username;
    private String phone;
    private String email;

    @JsonProperty("role_id")
    private Long roleId;

    @JsonProperty("role_name")
    private String roleName;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("onboarding_stage")
    private Integer onboardingStage;

    @JsonProperty("is_verified")
    private Boolean isVerified;

    @JsonProperty("last_login")
    private String lastLogin;

    @JsonProperty("accessToken")
    private String accessToken;

    private Long exp;
    private Object extend;

    @JsonProperty("must_change_password")
    private Boolean mustChangePassword;

    @JsonProperty("billing_currency")
    private BillingCurrency billingCurrency;

    public AuthResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public Integer getOnboardingStage() { return onboardingStage; }
    public void setOnboardingStage(Integer onboardingStage) { this.onboardingStage = onboardingStage; }
    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public Long getExp() { return exp; }
    public void setExp(Long exp) { this.exp = exp; }
    public Object getExtend() { return extend; }
    public void setExtend(Object extend) { this.extend = extend; }
    public BillingCurrency getBillingCurrency() { return billingCurrency; }
    public void setBillingCurrency(BillingCurrency billingCurrency) { this.billingCurrency = billingCurrency; }
    public Boolean getMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(Boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }
}
