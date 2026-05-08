package com.TravelMedicineAdvisory.Server.domain.affiliate;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlan;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;

import java.math.BigDecimal;

@Entity
@Table(name = "affiliate_referral_links", indexes = {
        @Index(name = "idx_affiliate_referral_links_affiliate", columnList = "affiliate_id"),
        @Index(name = "idx_affiliate_referral_links_short_code", columnList = "short_code"),
        @Index(name = "idx_affiliate_referral_links_plan", columnList = "credit_plan_id")
})
@SQLDelete(sql = "UPDATE affiliate_referral_links SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class ReferralLink extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affiliate_id", nullable = false)
    private AffiliateProfile affiliateProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_plan_id")
    private CreditPlan creditPlan;

    @Column(nullable = false, length = 120)
    private String campaign;

    @Column(name = "destination_url", nullable = false, columnDefinition = "TEXT")
    private String destinationUrl;

    @Column(name = "short_code", nullable = false, unique = true, length = 40)
    private String shortCode;

    @Column(nullable = false)
    private Integer clicks = 0;

    @Column(nullable = false)
    private Integer conversions = 0;

    @Column(name = "commission_earned", nullable = false, precision = 12, scale = 2)
    private BigDecimal commissionEarned = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public AffiliateProfile getAffiliateProfile() {
        return affiliateProfile;
    }

    public void setAffiliateProfile(AffiliateProfile affiliateProfile) {
        this.affiliateProfile = affiliateProfile;
    }

    public CreditPlan getCreditPlan() {
        return creditPlan;
    }

    public void setCreditPlan(CreditPlan creditPlan) {
        this.creditPlan = creditPlan;
    }

    public String getCampaign() {
        return campaign;
    }

    public void setCampaign(String campaign) {
        this.campaign = campaign;
    }

    public String getDestinationUrl() {
        return destinationUrl;
    }

    public void setDestinationUrl(String destinationUrl) {
        this.destinationUrl = destinationUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public Integer getClicks() {
        return clicks;
    }

    public void setClicks(Integer clicks) {
        this.clicks = clicks;
    }

    public Integer getConversions() {
        return conversions;
    }

    public void setConversions(Integer conversions) {
        this.conversions = conversions;
    }

    public BigDecimal getCommissionEarned() {
        return commissionEarned;
    }

    public void setCommissionEarned(BigDecimal commissionEarned) {
        this.commissionEarned = commissionEarned;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
