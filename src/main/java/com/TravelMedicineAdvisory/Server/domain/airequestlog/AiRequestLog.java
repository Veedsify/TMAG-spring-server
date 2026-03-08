package com.TravelMedicineAdvisory.Server.domain.airequestlog;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import java.math.BigDecimal;

@Entity
@Table(name = "ai_request_logs")
@SQLDelete(sql = "UPDATE ai_request_logs SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class AiRequestLog extends BaseEntity {

    private String destination;
    @Column(name = "prompt_summary", columnDefinition = "TEXT")
    private String promptSummary;
    @Column(name = "output_summary", columnDefinition = "TEXT")
    private String outputSummary;
    @Column(name = "tokens_used")
    private Integer tokensUsed;
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;
    private String status;
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    @Column(name = "risk_level")
    private String riskLevel;
    @Column(name = "model_used")
    private String modelUsed;
    @Column(name = "credit_consumed", precision = 10, scale = 2)
    private BigDecimal creditConsumed;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getPromptSummary() {
        return promptSummary;
    }

    public void setPromptSummary(String promptSummary) {
        this.promptSummary = promptSummary;
    }

    public String getOutputSummary() {
        return outputSummary;
    }

    public void setOutputSummary(String outputSummary) {
        this.outputSummary = outputSummary;
    }

    public Integer getTokensUsed() {
        return tokensUsed;
    }

    public void setTokensUsed(Integer tokensUsed) {
        this.tokensUsed = tokensUsed;
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
    }

    public BigDecimal getCreditConsumed() {
        return creditConsumed;
    }

    public void setCreditConsumed(BigDecimal creditConsumed) {
        this.creditConsumed = creditConsumed;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
