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

    public AdminSystemSettingsResponse() {}

    public AdminSystemSettingsResponse(Integer defaultIndividualCredits, Integer defaultCorporateCredits,
                                        String aiModelVersion, Integer planGenerationLimit,
                                        String globalDisclaimer, Boolean maintenanceMode,
                                        Boolean emailNotifications, Integer maxEmployeesPerCompany) {
        this.defaultIndividualCredits = defaultIndividualCredits;
        this.defaultCorporateCredits = defaultCorporateCredits;
        this.aiModelVersion = aiModelVersion;
        this.planGenerationLimit = planGenerationLimit;
        this.globalDisclaimer = globalDisclaimer;
        this.maintenanceMode = maintenanceMode;
        this.emailNotifications = emailNotifications;
        this.maxEmployeesPerCompany = maxEmployeesPerCompany;
    }

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
}
