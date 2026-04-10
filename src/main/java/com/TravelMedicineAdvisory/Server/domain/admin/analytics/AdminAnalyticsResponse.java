package com.TravelMedicineAdvisory.Server.domain.admin.analytics;

import java.util.List;
import java.util.Map;

public class AdminAnalyticsResponse {
    private List<Map<String, Object>> topDestinations;
    private Double avgCreditsPerUser;
    private Map<String, Long> corporateVsIndividual;
    private List<Map<String, Object>> peakUsageTimes;
    private List<Map<String, Object>> monthlyRequests;
    private List<Map<String, Object>> dailyActiveUsers;
    private List<Map<String, Object>> creditUsageByType;
    private List<Map<String, Object>> requestsByModel;
    private List<Map<String, Object>> riskDistribution;

    public AdminAnalyticsResponse() {}

    public List<Map<String, Object>> getTopDestinations() { return topDestinations; }
    public void setTopDestinations(List<Map<String, Object>> topDestinations) { this.topDestinations = topDestinations; }
    public Double getAvgCreditsPerUser() { return avgCreditsPerUser; }
    public void setAvgCreditsPerUser(Double avgCreditsPerUser) { this.avgCreditsPerUser = avgCreditsPerUser; }
    public Map<String, Long> getCorporateVsIndividual() { return corporateVsIndividual; }
    public void setCorporateVsIndividual(Map<String, Long> corporateVsIndividual) { this.corporateVsIndividual = corporateVsIndividual; }
    public List<Map<String, Object>> getPeakUsageTimes() { return peakUsageTimes; }
    public void setPeakUsageTimes(List<Map<String, Object>> peakUsageTimes) { this.peakUsageTimes = peakUsageTimes; }
    public List<Map<String, Object>> getMonthlyRequests() { return monthlyRequests; }
    public void setMonthlyRequests(List<Map<String, Object>> monthlyRequests) { this.monthlyRequests = monthlyRequests; }
    public List<Map<String, Object>> getDailyActiveUsers() { return dailyActiveUsers; }
    public void setDailyActiveUsers(List<Map<String, Object>> dailyActiveUsers) { this.dailyActiveUsers = dailyActiveUsers; }
    public List<Map<String, Object>> getCreditUsageByType() { return creditUsageByType; }
    public void setCreditUsageByType(List<Map<String, Object>> creditUsageByType) { this.creditUsageByType = creditUsageByType; }
    public List<Map<String, Object>> getRequestsByModel() { return requestsByModel; }
    public void setRequestsByModel(List<Map<String, Object>> requestsByModel) { this.requestsByModel = requestsByModel; }
    public List<Map<String, Object>> getRiskDistribution() { return riskDistribution; }
    public void setRiskDistribution(List<Map<String, Object>> riskDistribution) { this.riskDistribution = riskDistribution; }
}
