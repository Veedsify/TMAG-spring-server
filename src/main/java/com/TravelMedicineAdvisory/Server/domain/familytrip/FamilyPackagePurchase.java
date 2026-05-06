package com.TravelMedicineAdvisory.Server.domain.familytrip;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import com.TravelMedicineAdvisory.Server.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "family_package_purchases", indexes = {
    @Index(name = "idx_family_purchases_user_status", columnList = "user_id, status")
})
@SQLDelete(sql = "UPDATE family_package_purchases SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class FamilyPackagePurchase extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "package_type", nullable = false)
    private FamilyPackageType packageType;

    @Column(name = "trips_allowed", nullable = false)
    private Integer tripsAllowed;

    @Column(name = "trips_used", nullable = false)
    private Integer tripsUsed = 0;

    @Column(name = "amount_paid_minor", nullable = false)
    private Long amountPaidMinor;

    @Column(nullable = false)
    private String currency;

    @Column(name = "payment_provider")
    private String paymentProvider;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FamilyPackagePurchaseStatus status;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "tx_ref", nullable = false, unique = true)
    private String txRef;

    @Column(name = "flw_ref")
    private String flwRef;

    @Column(name = "flutterwave_status")
    private String flutterwaveStatus;

    @Column(name = "failed_reason")
    private String failedReason;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public FamilyPackageType getPackageType() { return packageType; }
    public void setPackageType(FamilyPackageType packageType) { this.packageType = packageType; }

    public Integer getTripsAllowed() { return tripsAllowed; }
    public void setTripsAllowed(Integer tripsAllowed) { this.tripsAllowed = tripsAllowed; }

    public Integer getTripsUsed() { return tripsUsed; }
    public void setTripsUsed(Integer tripsUsed) { this.tripsUsed = tripsUsed; }

    public Long getAmountPaidMinor() { return amountPaidMinor; }
    public void setAmountPaidMinor(Long amountPaidMinor) { this.amountPaidMinor = amountPaidMinor; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getPaymentProvider() { return paymentProvider; }
    public void setPaymentProvider(String paymentProvider) { this.paymentProvider = paymentProvider; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public FamilyPackagePurchaseStatus getStatus() { return status; }
    public void setStatus(FamilyPackagePurchaseStatus status) { this.status = status; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getTxRef() { return txRef; }
    public void setTxRef(String txRef) { this.txRef = txRef; }

    public String getFlwRef() { return flwRef; }
    public void setFlwRef(String flwRef) { this.flwRef = flwRef; }

    public String getFlutterwaveStatus() { return flutterwaveStatus; }
    public void setFlutterwaveStatus(String flutterwaveStatus) { this.flutterwaveStatus = flutterwaveStatus; }

    public String getFailedReason() { return failedReason; }
    public void setFailedReason(String failedReason) { this.failedReason = failedReason; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}
