package com.TravelMedicineAdvisory.Server.domain.affiliate;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;

import java.math.BigDecimal;

@Entity
@Table(name = "affiliate_profiles", indexes = {
        @Index(name = "idx_affiliate_profiles_user", columnList = "user_id"),
        @Index(name = "idx_affiliate_profiles_referral_code", columnList = "referral_code"),
        @Index(name = "idx_affiliate_profiles_status", columnList = "status")
})
@SQLDelete(sql = "UPDATE affiliate_profiles SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class AffiliateProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "referral_code", nullable = false, unique = true, length = 40)
    private String referralCode;

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate = BigDecimal.valueOf(10);

    @Column(name = "discount_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountRate = BigDecimal.valueOf(5);

    @Column(name = "total_clicks", nullable = false)
    private Integer totalClicks = 0;

    @Column(name = "total_conversions", nullable = false)
    private Integer totalConversions = 0;

    @Column(name = "total_commission_earned", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCommissionEarned = BigDecimal.ZERO;

    @Column(name = "total_paid_out", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPaidOut = BigDecimal.ZERO;

    @Column(name = "pending_commission", nullable = false, precision = 12, scale = 2)
    private BigDecimal pendingCommission = BigDecimal.ZERO;

    @Column(name = "total_commission_earned_ngn", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCommissionEarnedNgn = BigDecimal.ZERO;

    @Column(name = "pending_commission_ngn", nullable = false, precision = 12, scale = 2)
    private BigDecimal pendingCommissionNgn = BigDecimal.ZERO;

    @Column(name = "total_paid_out_ngn", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPaidOutNgn = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    private String status = "active";

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    public Integer getTotalClicks() {
        return totalClicks;
    }

    public void setTotalClicks(Integer totalClicks) {
        this.totalClicks = totalClicks;
    }

    public Integer getTotalConversions() {
        return totalConversions;
    }

    public void setTotalConversions(Integer totalConversions) {
        this.totalConversions = totalConversions;
    }

    public BigDecimal getTotalCommissionEarned() {
        return totalCommissionEarned;
    }

    public void setTotalCommissionEarned(BigDecimal totalCommissionEarned) {
        this.totalCommissionEarned = totalCommissionEarned;
    }

    public BigDecimal getTotalPaidOut() {
        return totalPaidOut;
    }

    public void setTotalPaidOut(BigDecimal totalPaidOut) {
        this.totalPaidOut = totalPaidOut;
    }

    public BigDecimal getPendingCommission() {
        return pendingCommission;
    }

    public void setPendingCommission(BigDecimal pendingCommission) {
        this.pendingCommission = pendingCommission;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalCommissionEarnedNgn() {
        return totalCommissionEarnedNgn;
    }

    public void setTotalCommissionEarnedNgn(BigDecimal totalCommissionEarnedNgn) {
        this.totalCommissionEarnedNgn = totalCommissionEarnedNgn;
    }

    public BigDecimal getPendingCommissionNgn() {
        return pendingCommissionNgn;
    }

    public void setPendingCommissionNgn(BigDecimal pendingCommissionNgn) {
        this.pendingCommissionNgn = pendingCommissionNgn;
    }

    public BigDecimal getTotalPaidOutNgn() {
        return totalPaidOutNgn;
    }

    public void setTotalPaidOutNgn(BigDecimal totalPaidOutNgn) {
        this.totalPaidOutNgn = totalPaidOutNgn;
    }
}
