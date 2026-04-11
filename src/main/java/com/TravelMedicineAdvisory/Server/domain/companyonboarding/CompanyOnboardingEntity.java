package com.TravelMedicineAdvisory.Server.domain.companyonboarding;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "company_onboarding_requests")
@SQLDelete(sql = "UPDATE company_onboarding_requests SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class CompanyOnboardingEntity extends BaseEntity {

    @Column(name = "company_name", nullable = false)
    private String companyName;

    private String industry;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    private String website;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_currency", nullable = false)
    private BillingCurrency billingCurrency = BillingCurrency.USD;

    @Column(name = "selected_plan_code", nullable = false)
    private String selectedPlanCode;

    @Column(name = "sample_request", columnDefinition = "TEXT")
    private String sampleRequest;

    @Column(name = "team_members", columnDefinition = "TEXT", nullable = false)
    private String teamMembers = "[]";

    @Column(name = "tx_ref", unique = true)
    private String txRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private OnboardingPaymentStatus paymentStatus = OnboardingPaymentStatus.PENDING;

    @Column(name = "payment_amount")
    private BigDecimal paymentAmount;

    @Column(name = "payment_currency")
    private String paymentCurrency;

    @Column(name = "flw_ref")
    private String flwRef;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OnboardingStatus status = OnboardingStatus.PENDING_PAYMENT;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_company_id")
    private Long createdCompanyId;

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public BillingCurrency getBillingCurrency() { return billingCurrency; }
    public void setBillingCurrency(BillingCurrency billingCurrency) { this.billingCurrency = billingCurrency; }

    public String getSelectedPlanCode() { return selectedPlanCode; }
    public void setSelectedPlanCode(String selectedPlanCode) { this.selectedPlanCode = selectedPlanCode; }

    public String getSampleRequest() { return sampleRequest; }
    public void setSampleRequest(String sampleRequest) { this.sampleRequest = sampleRequest; }

    public String getTeamMembers() { return teamMembers; }
    public void setTeamMembers(String teamMembers) { this.teamMembers = teamMembers; }

    public String getTxRef() { return txRef; }
    public void setTxRef(String txRef) { this.txRef = txRef; }

    public OnboardingPaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(OnboardingPaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(BigDecimal paymentAmount) { this.paymentAmount = paymentAmount; }

    public String getPaymentCurrency() { return paymentCurrency; }
    public void setPaymentCurrency(String paymentCurrency) { this.paymentCurrency = paymentCurrency; }

    public String getFlwRef() { return flwRef; }
    public void setFlwRef(String flwRef) { this.flwRef = flwRef; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public OnboardingStatus getStatus() { return status; }
    public void setStatus(OnboardingStatus status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public Long getCreatedCompanyId() { return createdCompanyId; }
    public void setCreatedCompanyId(Long createdCompanyId) { this.createdCompanyId = createdCompanyId; }
}
