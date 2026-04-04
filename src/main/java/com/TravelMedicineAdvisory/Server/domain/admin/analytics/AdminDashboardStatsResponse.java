package com.TravelMedicineAdvisory.Server.domain.admin.analytics;


public class AdminDashboardStatsResponse {
    private Long totalUsers;
    private Long totalCompanies;
    private Long totalCreditsIssued;
    private Long totalCreditsConsumed;
    private Long aiRequestsToday;
    private Double revenueOverview;
    private String revenueBaseCurrency;
    private Long failedAICalls;
    private String systemHealthStatus;
    private Long activeUsersToday;
    private Long newUsersThisWeek;

    public AdminDashboardStatsResponse() {}

    public Long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }
    public Long getTotalCompanies() { return totalCompanies; }
    public void setTotalCompanies(Long totalCompanies) { this.totalCompanies = totalCompanies; }
    public Long getTotalCreditsIssued() { return totalCreditsIssued; }
    public void setTotalCreditsIssued(Long totalCreditsIssued) { this.totalCreditsIssued = totalCreditsIssued; }
    public Long getTotalCreditsConsumed() { return totalCreditsConsumed; }
    public void setTotalCreditsConsumed(Long totalCreditsConsumed) { this.totalCreditsConsumed = totalCreditsConsumed; }
    public Long getAiRequestsToday() { return aiRequestsToday; }
    public void setAiRequestsToday(Long aiRequestsToday) { this.aiRequestsToday = aiRequestsToday; }
    public Double getRevenueOverview() { return revenueOverview; }
    public void setRevenueOverview(Double revenueOverview) { this.revenueOverview = revenueOverview; }
    public String getRevenueBaseCurrency() { return revenueBaseCurrency; }
    public void setRevenueBaseCurrency(String revenueBaseCurrency) { this.revenueBaseCurrency = revenueBaseCurrency; }
    public Long getFailedAICalls() { return failedAICalls; }
    public void setFailedAICalls(Long failedAICalls) { this.failedAICalls = failedAICalls; }
    public String getSystemHealthStatus() { return systemHealthStatus; }
    public void setSystemHealthStatus(String systemHealthStatus) { this.systemHealthStatus = systemHealthStatus; }
    public Long getActiveUsersToday() { return activeUsersToday; }
    public void setActiveUsersToday(Long activeUsersToday) { this.activeUsersToday = activeUsersToday; }
    public Long getNewUsersThisWeek() { return newUsersThisWeek; }
    public void setNewUsersThisWeek(Long newUsersThisWeek) { this.newUsersThisWeek = newUsersThisWeek; }
}
