package com.TravelMedicineAdvisory.Server.domain.affiliate;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;

@Entity
@Table(name = "affiliate_referrals", indexes = {
        @Index(name = "idx_affiliate_referrals_affiliate", columnList = "affiliate_id"),
        @Index(name = "idx_affiliate_referrals_link", columnList = "referral_link_id"),
        @Index(name = "idx_affiliate_referrals_referred_user", columnList = "referred_user_id")
})
@SQLDelete(sql = "UPDATE affiliate_referrals SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class AffiliateReferral extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affiliate_id", nullable = false)
    private AffiliateProfile affiliateProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referral_link_id")
    private ReferralLink referralLink;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referred_user_id", nullable = false, unique = true)
    private User referredUser;

    @Column(name = "referral_code", nullable = false, length = 40)
    private String referralCode;

    @Column(nullable = false, length = 20)
    private String status = "active";

    @Column(name = "first_click_at")
    private LocalDateTime firstClickAt;

    @Column(name = "converted_at")
    private LocalDateTime convertedAt;

    public AffiliateProfile getAffiliateProfile() {
        return affiliateProfile;
    }

    public void setAffiliateProfile(AffiliateProfile affiliateProfile) {
        this.affiliateProfile = affiliateProfile;
    }

    public ReferralLink getReferralLink() {
        return referralLink;
    }

    public void setReferralLink(ReferralLink referralLink) {
        this.referralLink = referralLink;
    }

    public User getReferredUser() {
        return referredUser;
    }

    public void setReferredUser(User referredUser) {
        this.referredUser = referredUser;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getFirstClickAt() {
        return firstClickAt;
    }

    public void setFirstClickAt(LocalDateTime firstClickAt) {
        this.firstClickAt = firstClickAt;
    }

    public LocalDateTime getConvertedAt() {
        return convertedAt;
    }

    public void setConvertedAt(LocalDateTime convertedAt) {
        this.convertedAt = convertedAt;
    }
}
