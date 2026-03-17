package com.TravelMedicineAdvisory.Server.domain.admin.users;

import java.time.LocalDateTime;
import java.util.List;

public class AdminUserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String planType;
    private Long companyId;
    private String companyName;
    private Integer creditsUsed;
    private Integer creditsRemaining;
    private Integer plansGenerated;
    private LocalDateTime lastActivity;
    private String status;
    private List<String> riskFlags;
    private LocalDateTime joinedAt;
    private String avatar;
    private String location;
    private String bio;

    public AdminUserResponse() {}

    public AdminUserResponse(Long id, String name, String email, String phone, String role, 
                             String planType, Long companyId, String companyName, Integer creditsUsed,
                             Integer creditsRemaining, Integer plansGenerated, LocalDateTime lastActivity,
                             String status, List<String> riskFlags, LocalDateTime joinedAt, 
                             String avatar, String location, String bio) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.planType = planType;
        this.companyId = companyId;
        this.companyName = companyName;
        this.creditsUsed = creditsUsed;
        this.creditsRemaining = creditsRemaining;
        this.plansGenerated = plansGenerated;
        this.lastActivity = lastActivity;
        this.status = status;
        this.riskFlags = riskFlags;
        this.joinedAt = joinedAt;
        this.avatar = avatar;
        this.location = location;
        this.bio = bio;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public Integer getCreditsUsed() { return creditsUsed; }
    public void setCreditsUsed(Integer creditsUsed) { this.creditsUsed = creditsUsed; }
    public Integer getCreditsRemaining() { return creditsRemaining; }
    public void setCreditsRemaining(Integer creditsRemaining) { this.creditsRemaining = creditsRemaining; }
    public Integer getPlansGenerated() { return plansGenerated; }
    public void setPlansGenerated(Integer plansGenerated) { this.plansGenerated = plansGenerated; }
    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getRiskFlags() { return riskFlags; }
    public void setRiskFlags(List<String> riskFlags) { this.riskFlags = riskFlags; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
