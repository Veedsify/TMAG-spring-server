package com.TravelMedicineAdvisory.Server.domain.admin.analytics;

import java.time.LocalDateTime;

public class AdminAIRequestLogResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long companyId;
    private String companyName;
    private String destination;
    private String promptSummary;
    private String outputSummary;
    private Integer tokensUsed;
    private Integer planGenerationTokensUsed;
    private Integer summaryGenerationTokensUsed;
    private Long processingTimeMs;
    private String status;
    private String errorMessage;
    private String riskLevel;
    private LocalDateTime timestamp;
    private String modelUsed;
    private Boolean creditConsumed;

    public AdminAIRequestLogResponse() {}

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
    public String getPromptSummary() { return promptSummary; }
    public void setPromptSummary(String promptSummary) { this.promptSummary = promptSummary; }
    public String getOutputSummary() { return outputSummary; }
    public void setOutputSummary(String outputSummary) { this.outputSummary = outputSummary; }
    public Integer getTokensUsed() { return tokensUsed; }
    public void setTokensUsed(Integer tokensUsed) { this.tokensUsed = tokensUsed; }
    public Integer getPlanGenerationTokensUsed() { return planGenerationTokensUsed; }
    public void setPlanGenerationTokensUsed(Integer planGenerationTokensUsed) { this.planGenerationTokensUsed = planGenerationTokensUsed; }
    public Integer getSummaryGenerationTokensUsed() { return summaryGenerationTokensUsed; }
    public void setSummaryGenerationTokensUsed(Integer summaryGenerationTokensUsed) { this.summaryGenerationTokensUsed = summaryGenerationTokensUsed; }
    public Long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }
    public Boolean getCreditConsumed() { return creditConsumed; }
    public void setCreditConsumed(Boolean creditConsumed) { this.creditConsumed = creditConsumed; }
}
