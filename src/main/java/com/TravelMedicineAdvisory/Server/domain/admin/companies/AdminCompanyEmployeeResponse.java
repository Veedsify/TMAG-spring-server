package com.TravelMedicineAdvisory.Server.domain.admin.companies;

public class AdminCompanyEmployeeResponse {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String department;
    private String status;
    private Integer creditsAllocated;
    private Integer creditsUsed;
    private Integer plansGenerated;
    private String avatar;

    public AdminCompanyEmployeeResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getCreditsAllocated() { return creditsAllocated; }
    public void setCreditsAllocated(Integer creditsAllocated) { this.creditsAllocated = creditsAllocated; }
    public Integer getCreditsUsed() { return creditsUsed; }
    public void setCreditsUsed(Integer creditsUsed) { this.creditsUsed = creditsUsed; }
    public Integer getPlansGenerated() { return plansGenerated; }
    public void setPlansGenerated(Integer plansGenerated) { this.plansGenerated = plansGenerated; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}
