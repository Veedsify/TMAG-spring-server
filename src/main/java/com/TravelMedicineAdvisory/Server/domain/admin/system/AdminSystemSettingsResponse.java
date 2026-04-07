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
    private Double exchangeRateINR;
    private Double exchangeRateCAD;
    private Double exchangeRateAUD;
    private Double exchangeRateKES;
    private Double exchangeRateZAR;
    private Double exchangeRateGHS;
    private Double exchangeRateJPY;
    private Double exchangeRateBRL;
    private Double exchangeRateAED;
    private Double exchangeRateSGD;
    private Double exchangeRateCHF;

    public AdminSystemSettingsResponse() {}

    public static AdminSystemSettingsResponse fromMap(Map<String, Object> settings) {
        AdminSystemSettingsResponse response = new AdminSystemSettingsResponse();
        setInt(settings, "defaultIndividualCredits", v -> response.defaultIndividualCredits = v);
        setInt(settings, "defaultCorporateCredits", v -> response.defaultCorporateCredits = v);
        setStr(settings, "aiModelVersion", v -> response.aiModelVersion = v);
        setInt(settings, "planGenerationLimit", v -> response.planGenerationLimit = v);
        setStr(settings, "globalDisclaimer", v -> response.globalDisclaimer = v);
        setBool(settings, "maintenanceMode", v -> response.maintenanceMode = v);
        setBool(settings, "emailNotifications", v -> response.emailNotifications = v);
        setInt(settings, "maxEmployeesPerCompany", v -> response.maxEmployeesPerCompany = v);
        setStr(settings, "revenueBaseCurrency", v -> response.revenueBaseCurrency = v);
        setDbl(settings, "exchangeRateNGN", v -> response.exchangeRateNGN = v);
        setDbl(settings, "exchangeRateEUR", v -> response.exchangeRateEUR = v);
        setDbl(settings, "exchangeRateGBP", v -> response.exchangeRateGBP = v);
        setDbl(settings, "exchangeRateINR", v -> response.exchangeRateINR = v);
        setDbl(settings, "exchangeRateCAD", v -> response.exchangeRateCAD = v);
        setDbl(settings, "exchangeRateAUD", v -> response.exchangeRateAUD = v);
        setDbl(settings, "exchangeRateKES", v -> response.exchangeRateKES = v);
        setDbl(settings, "exchangeRateZAR", v -> response.exchangeRateZAR = v);
        setDbl(settings, "exchangeRateGHS", v -> response.exchangeRateGHS = v);
        setDbl(settings, "exchangeRateJPY", v -> response.exchangeRateJPY = v);
        setDbl(settings, "exchangeRateBRL", v -> response.exchangeRateBRL = v);
        setDbl(settings, "exchangeRateAED", v -> response.exchangeRateAED = v);
        setDbl(settings, "exchangeRateSGD", v -> response.exchangeRateSGD = v);
        setDbl(settings, "exchangeRateCHF", v -> response.exchangeRateCHF = v);
        return response;
    }

    private static void setInt(Map<String, Object> m, String key, java.util.function.Consumer<Integer> setter) {
        if (m.containsKey(key) && m.get(key) instanceof Number n) setter.accept(n.intValue());
    }
    private static void setStr(Map<String, Object> m, String key, java.util.function.Consumer<String> setter) {
        if (m.containsKey(key) && m.get(key) instanceof String s) setter.accept(s);
    }
    private static void setBool(Map<String, Object> m, String key, java.util.function.Consumer<Boolean> setter) {
        if (m.containsKey(key) && m.get(key) instanceof Boolean b) setter.accept(b);
    }
    private static void setDbl(Map<String, Object> m, String key, java.util.function.Consumer<Double> setter) {
        if (m.containsKey(key) && m.get(key) instanceof Number n) setter.accept(n.doubleValue());
    }

    // Getters and setters
    public Integer getDefaultIndividualCredits() { return defaultIndividualCredits; }
    public void setDefaultIndividualCredits(Integer v) { this.defaultIndividualCredits = v; }
    public Integer getDefaultCorporateCredits() { return defaultCorporateCredits; }
    public void setDefaultCorporateCredits(Integer v) { this.defaultCorporateCredits = v; }
    public String getAiModelVersion() { return aiModelVersion; }
    public void setAiModelVersion(String v) { this.aiModelVersion = v; }
    public Integer getPlanGenerationLimit() { return planGenerationLimit; }
    public void setPlanGenerationLimit(Integer v) { this.planGenerationLimit = v; }
    public String getGlobalDisclaimer() { return globalDisclaimer; }
    public void setGlobalDisclaimer(String v) { this.globalDisclaimer = v; }
    public Boolean getMaintenanceMode() { return maintenanceMode; }
    public void setMaintenanceMode(Boolean v) { this.maintenanceMode = v; }
    public Boolean getEmailNotifications() { return emailNotifications; }
    public void setEmailNotifications(Boolean v) { this.emailNotifications = v; }
    public Integer getMaxEmployeesPerCompany() { return maxEmployeesPerCompany; }
    public void setMaxEmployeesPerCompany(Integer v) { this.maxEmployeesPerCompany = v; }
    public String getRevenueBaseCurrency() { return revenueBaseCurrency; }
    public void setRevenueBaseCurrency(String v) { this.revenueBaseCurrency = v; }
    public Double getExchangeRateNGN() { return exchangeRateNGN; }
    public void setExchangeRateNGN(Double v) { this.exchangeRateNGN = v; }
    public Double getExchangeRateEUR() { return exchangeRateEUR; }
    public void setExchangeRateEUR(Double v) { this.exchangeRateEUR = v; }
    public Double getExchangeRateGBP() { return exchangeRateGBP; }
    public void setExchangeRateGBP(Double v) { this.exchangeRateGBP = v; }
    public Double getExchangeRateINR() { return exchangeRateINR; }
    public void setExchangeRateINR(Double v) { this.exchangeRateINR = v; }
    public Double getExchangeRateCAD() { return exchangeRateCAD; }
    public void setExchangeRateCAD(Double v) { this.exchangeRateCAD = v; }
    public Double getExchangeRateAUD() { return exchangeRateAUD; }
    public void setExchangeRateAUD(Double v) { this.exchangeRateAUD = v; }
    public Double getExchangeRateKES() { return exchangeRateKES; }
    public void setExchangeRateKES(Double v) { this.exchangeRateKES = v; }
    public Double getExchangeRateZAR() { return exchangeRateZAR; }
    public void setExchangeRateZAR(Double v) { this.exchangeRateZAR = v; }
    public Double getExchangeRateGHS() { return exchangeRateGHS; }
    public void setExchangeRateGHS(Double v) { this.exchangeRateGHS = v; }
    public Double getExchangeRateJPY() { return exchangeRateJPY; }
    public void setExchangeRateJPY(Double v) { this.exchangeRateJPY = v; }
    public Double getExchangeRateBRL() { return exchangeRateBRL; }
    public void setExchangeRateBRL(Double v) { this.exchangeRateBRL = v; }
    public Double getExchangeRateAED() { return exchangeRateAED; }
    public void setExchangeRateAED(Double v) { this.exchangeRateAED = v; }
    public Double getExchangeRateSGD() { return exchangeRateSGD; }
    public void setExchangeRateSGD(Double v) { this.exchangeRateSGD = v; }
    public Double getExchangeRateCHF() { return exchangeRateCHF; }
    public void setExchangeRateCHF(Double v) { this.exchangeRateCHF = v; }
}
