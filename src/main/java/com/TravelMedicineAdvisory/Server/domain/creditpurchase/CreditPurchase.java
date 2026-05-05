package com.TravelMedicineAdvisory.Server.domain.creditpurchase;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;

import java.math.BigDecimal;

@Entity
@Table(name = "credit_purchases", indexes = {
    @Index(name = "idx_credit_purchases_user_created", columnList = "user_id, created_at"),
    @Index(name = "idx_credit_purchases_company_created", columnList = "company_id, created_at"),
    @Index(name = "idx_credit_purchases_status_created", columnList = "status, created_at"),
    @Index(name = "idx_credit_purchases_user_status", columnList = "user_id, status")
})
@SQLDelete(sql = "UPDATE credit_purchases SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")

public class CreditPurchase extends BaseEntity {

    @Column(name = "tx_ref", nullable = false, unique = true)
    private String txRef;

    @Column(name = "flw_ref")
    private String flwRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "credits_purchased", nullable = false)
    private Integer creditsPurchased;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)

    private BillingCurrency currency;

    @Column(name = "currency_symbol")
    private String currencySymbol;

    @Column(name = "price_per_credit", precision = 10, scale = 2)
    private BigDecimal pricePerCredit;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "amount_paid", precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Column(nullable = false)
    private String status;

    @Column(name = "flutterwave_status")
    private String flutterwaveStatus;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "paid_at")
    private java.time.LocalDateTime paidAt;

    @Column(name = "failed_at")
    private java.time.LocalDateTime failedAt;

    @Column(name = "failed_reason")
    private String failedReason;

    public String getTxRef() {
        return txRef;
    }

    public void setTxRef(String txRef) {
        this.txRef = txRef;
    }

    public String getFlwRef() {
        return flwRef;
    }

    public void setFlwRef(String flwRef) {
        this.flwRef = flwRef;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Integer getCreditsPurchased() {
        return creditsPurchased;
    }

    public void setCreditsPurchased(Integer creditsPurchased) {
        this.creditsPurchased = creditsPurchased;
    }

    public BillingCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(BillingCurrency currency) {
        this.currency = currency;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public BigDecimal getPricePerCredit() {
        return pricePerCredit;
    }

    public void setPricePerCredit(BigDecimal pricePerCredit) {
        this.pricePerCredit = pricePerCredit;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFlutterwaveStatus() {
        return flutterwaveStatus;
    }

    public void setFlutterwaveStatus(String flutterwaveStatus) {
        this.flutterwaveStatus = flutterwaveStatus;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public java.time.LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(java.time.LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public java.time.LocalDateTime getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(java.time.LocalDateTime failedAt) {
        this.failedAt = failedAt;
    }

    public String getFailedReason() {
        return failedReason;
    }

    public void setFailedReason(String failedReason) {
        this.failedReason = failedReason;
    }
}
