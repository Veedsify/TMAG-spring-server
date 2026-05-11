package com.TravelMedicineAdvisory.Server.domain.affiliate;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "affiliate_clicks", indexes = {
        @Index(name = "idx_affiliate_clicks_affiliate", columnList = "affiliate_id, created_at"),
        @Index(name = "idx_affiliate_clicks_link", columnList = "referral_link_id, created_at")
})
@SQLDelete(sql = "UPDATE affiliate_clicks SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class AffiliateClick extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affiliate_id", nullable = false)
    private AffiliateProfile affiliateProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referral_link_id", nullable = false)
    private ReferralLink referralLink;

    @Column(name = "ip_address", length = 80)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

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

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
