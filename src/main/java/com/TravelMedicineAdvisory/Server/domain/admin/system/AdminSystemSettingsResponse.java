package com.TravelMedicineAdvisory.Server.domain.admin.system;

import java.util.Map;

public class AdminSystemSettingsResponse {
    private Integer defaultIndividualCredits;
    private Integer defaultCorporateCredits;
    private String aiModelVersion;
    private Integer planGenerationLimit;
    private String globalDisclaimer;
    private Boolean maintenanceMode;
    private Boolean emailNotifications;
    private Integer maxEmployeesPerCompany;
    private String revenueBaseCurrency;
    private Double exchangeRateNGN;
    private Double exchangeRateEUR;
    private Double exchangeRateGBP;

    public AdminSystemSettingsResponse() {}

    public static AdminSystemSettingsResponse fromMap(Map<String, Object> settings) {
        AdminSystemSettingsResponse response = new AdminSystemSettingsResponse();
        if (settings.containsKey("defaultIndividualCredits")) {
            response.setDefaultIndividualCredits((Integer) settings.get("defaultIndividualCredits"));
        }
        if (settings.containsKey("defaultCorporateCredits")) {
            response.setDefaultCorporateCredits((Integer) settings.get("defaultCorporateCredits"));
        }
        if (settings.containsKey("aiModelVersion")) {
            response.setAiModelVersion((String) settings.get("aiModelVersion"));
        }
        if (settings.containsKey("planGenerationLimit")) {
            response.setPlanGenerationLimit((Integer) settings.get("planGenerationLimit"));
        }
        if (settings.containsKey("globalDisclaimer")) {
            response.setGlobalDisclaimer((String) settings.get("globalDisclaimer"));
        }
        if (settings.containsKey("maintenanceMode")) {
            response.setMaintenanceMode((Boolean) settings.get("maintenanceMode"));
        }
        if (settings.containsKey("emailNotifications")) {
            response.setEmailNotifications((Boolean) settings.get("emailNotifications"));
        }
        if (settings.containsKey("maxEmployeesPerCompany")) {
            response.setMaxEmployeesPerCompany((Integer) settings.get("maxEmployeesPerCompany"));
        }
        if (settings.containsKey("revenueBaseCurrency")) {
            response.setRevenueBaseCurrency((String) settings.get("revenueBaseCurrency"));
        }
        if (settings.containsKey("exchangeRateNGN") && settings.get("exchangeRateNGN") instanceof Number) {
            response.setExchangeRateNGN(((Number) settings.get("exchangeRateNGN")).doubleValue());
        }
        if (settings.containsKey("exchangeRateEUR") && settings.get("exchangeRateEUR") instanceof Number) {
            response.setExchangeRateEUR(((Number) settings.get("exchangeRateEUR")).doubleValue());
        }
        if (settings.containsKey("exchangeRateGBP") && settings.get("exchangeRateGBP") instanceof Number) {
            response.setExchangeRateGBP(((Number) settings.get("exchangeRateGBP")).doubleValue());
        }
        return response;
    }

    public Integer getDefaultIndividualCredits() { return defaultIndividualCredits; }
    public void setDefaultIndividualCredits(Integer defaultIndividualCredits) { this.defaultIndividualCredits = defaultIndividualCredits; }
    public Integer getDefaultCorporateCredits() { return defaultCorporateCredits; }
    public void setDefaultCorporateCredits(Integer defaultCorporateCredits) { this.defaultCorporateCredits = defaultCorporateCredits; }
    public String getAiModelVersion() { return aiModelVersion; }
    public void setAiModelVersion(String aiModelVersion) { this.aiModelVersion = aiModelVersion; }
    public Integer getPlanGenerationLimit() { return planGenerationLimit; }
    public void setPlanGenerationLimit(Integer planGenerationLimit) { this.planGenerationLimit = planGenerationLimit; }
    public String getGlobalDisclaimer() { return globalDisclaimer; }
    public void setGlobalDisclaimer(String globalDisclaimer) { this.globalDisclaimer = globalDisclaimer; }
    public Boolean getMaintenanceMode() { return maintenanceMode; }
    public void setMaintenanceMode(Boolean maintenanceMode) { this.maintenanceMode = maintenanceMode; }
    public Boolean getEmailNotifications() { return emailNotifications; }
    public void setEmailNotifications(Boolean emailNotifications) { this.emailNotifications = emailNotifications; }
    public Integer getMaxEmployeesPerCompany() { return maxEmployeesPerCompany; }
    public void setMaxEmployeesPerCompany(Integer maxEmployeesPerCompany) { this.maxEmployeesPerCompany = maxEmployeesPerCompany; }
    public String getRevenueBaseCurrency() { return revenueBaseCurrency; }
    public void setRevenueBaseCurrency(String revenueBaseCurrency) { this.revenueBaseCurrency = revenueBaseCurrency; }
    public Double getExchangeRateNGN() { return exchangeRateNGN; }
    public void setExchangeRateNGN(Double exchangeRateNGN) { this.exchangeRateNGN = exchangeRateNGN; }
    public Double getExchangeRateEUR() { return exchangeRateEUR; }
    public void setExchangeRateEUR(Double exchangeRateEUR) { this.exchangeRateEUR = exchangeRateEUR; }
    public Double getExchangeRateGBP() { return exchangeRateGBP; }
    public void setExchangeRateGBP(Double exchangeRateGBP) { this.exchangeRateGBP = exchangeRateGBP; }
}
