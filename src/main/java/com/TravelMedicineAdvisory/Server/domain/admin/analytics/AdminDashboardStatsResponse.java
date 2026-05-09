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
    /** Active travel health plans (non-deleted). */
    private Long totalTravelPlans;
    /** Users with suspended accounts (soft-deleted / suspended). */
    private Long suspendedUsers;
    /** Invoices awaiting payment. */
    private Long pendingInvoicesCount;
    /** Company-linked employee records (active). */
    private Long totalEmployees;
    /** Abuse flags not marked resolved. */
    private Long unresolvedAbuseFlags;
    /** AI API calls in the rolling last 7 days. */
    private Long aiRequestsLast7Days;
    /** Share of AI logs with status success in the last 30 days (0–100). */
    private Double aiSuccessRateLast30Days;
    /** LLM tokens recorded on AI logs since midnight today. */
    private Long tokensUsedToday;
    /** AI logs with status error in the last 7 days. */
    private Long failedAiCallsLast7Days;

    private Double affiliateCommissionPaid;
    private Double affiliateCommissionPending;
    private Long totalActiveAffiliates;

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

    public Long getTotalTravelPlans() { return totalTravelPlans; }
    public void setTotalTravelPlans(Long totalTravelPlans) { this.totalTravelPlans = totalTravelPlans; }
    public Long getSuspendedUsers() { return suspendedUsers; }
    public void setSuspendedUsers(Long suspendedUsers) { this.suspendedUsers = suspendedUsers; }
    public Long getPendingInvoicesCount() { return pendingInvoicesCount; }
    public void setPendingInvoicesCount(Long pendingInvoicesCount) { this.pendingInvoicesCount = pendingInvoicesCount; }
    public Long getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(Long totalEmployees) { this.totalEmployees = totalEmployees; }
    public Long getUnresolvedAbuseFlags() { return unresolvedAbuseFlags; }
    public void setUnresolvedAbuseFlags(Long unresolvedAbuseFlags) { this.unresolvedAbuseFlags = unresolvedAbuseFlags; }
    public Long getAiRequestsLast7Days() { return aiRequestsLast7Days; }
    public void setAiRequestsLast7Days(Long aiRequestsLast7Days) { this.aiRequestsLast7Days = aiRequestsLast7Days; }
    public Double getAiSuccessRateLast30Days() { return aiSuccessRateLast30Days; }
    public void setAiSuccessRateLast30Days(Double aiSuccessRateLast30Days) { this.aiSuccessRateLast30Days = aiSuccessRateLast30Days; }
    public Long getTokensUsedToday() { return tokensUsedToday; }
    public void setTokensUsedToday(Long tokensUsedToday) { this.tokensUsedToday = tokensUsedToday; }
    public Long getFailedAiCallsLast7Days() { return failedAiCallsLast7Days; }
    public void setFailedAiCallsLast7Days(Long failedAiCallsLast7Days) { this.failedAiCallsLast7Days = failedAiCallsLast7Days; }

    public Double getAffiliateCommissionPaid() { return affiliateCommissionPaid; }
    public void setAffiliateCommissionPaid(Double affiliateCommissionPaid) { this.affiliateCommissionPaid = affiliateCommissionPaid; }
    public Double getAffiliateCommissionPending() { return affiliateCommissionPending; }
    public void setAffiliateCommissionPending(Double affiliateCommissionPending) { this.affiliateCommissionPending = affiliateCommissionPending; }
    public Long getTotalActiveAffiliates() { return totalActiveAffiliates; }
    public void setTotalActiveAffiliates(Long totalActiveAffiliates) { this.totalActiveAffiliates = totalActiveAffiliates; }
}
