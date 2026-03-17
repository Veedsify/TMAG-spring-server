package com.TravelMedicineAdvisory.Server.domain.admin.analytics;

import java.time.LocalDateTime;
import java.util.List;

public class AdminTravelPlanResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long companyId;
    private String companyName;
    private String destination;
    private String duration;
    private String purpose;
    private Integer riskScore;
    private List<String> vaccinations;
    private List<String> healthAlerts;
    private List<String> safetyAdvisories;
    private String status;
    private LocalDateTime createdAt;
    private Boolean creditUsed;

    public AdminTravelPlanResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }
    public List<String> getVaccinations() { return vaccinations; }
    public void setVaccinations(List<String> vaccinations) { this.vaccinations = vaccinations; }
    public List<String> getHealthAlerts() { return healthAlerts; }
    public void setHealthAlerts(List<String> healthAlerts) { this.healthAlerts = healthAlerts; }
    public List<String> getSafetyAdvisories() { return safetyAdvisories; }
    public void setSafetyAdvisories(List<String> safetyAdvisories) { this.safetyAdvisories = safetyAdvisories; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Boolean getCreditUsed() { return creditUsed; }
    public void setCreditUsed(Boolean creditUsed) { this.creditUsed = creditUsed; }
}
