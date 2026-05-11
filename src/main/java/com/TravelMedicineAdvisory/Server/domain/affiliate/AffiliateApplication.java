package com.TravelMedicineAdvisory.Server.domain.affiliate;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;

@Entity
@Table(name = "affiliate_applications", indexes = {
        @Index(name = "idx_affiliate_applications_email", columnList = "email"),
        @Index(name = "idx_affiliate_applications_status", columnList = "status")
})
@SQLDelete(sql = "UPDATE affiliate_applications SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class AffiliateApplication extends BaseEntity {

    @Column(nullable = false)
    private String fullName;

    @Column
    private String companyName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String phone;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "social_media_links")
    private String socialMediaLinks;

    @Column(name = "estimated_monthly_reach")
    private String estimatedMonthlyReach;

    @Column(columnDefinition = "TEXT")
    private String promoDescription;

    @Column(nullable = false)
    private Boolean agreedToTerms = true;

    @Column(nullable = false, length = 30)
    private String status = "pending";

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "admin_notes")
    private String adminNotes;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getSocialMediaLinks() {
        return socialMediaLinks;
    }

    public void setSocialMediaLinks(String socialMediaLinks) {
        this.socialMediaLinks = socialMediaLinks;
    }

    public String getEstimatedMonthlyReach() {
        return estimatedMonthlyReach;
    }

    public void setEstimatedMonthlyReach(String estimatedMonthlyReach) {
        this.estimatedMonthlyReach = estimatedMonthlyReach;
    }

    public String getPromoDescription() {
        return promoDescription;
    }

    public void setPromoDescription(String promoDescription) {
        this.promoDescription = promoDescription;
    }

    public Boolean getAgreedToTerms() {
        return agreedToTerms;
    }

    public void setAgreedToTerms(Boolean agreedToTerms) {
        this.agreedToTerms = agreedToTerms;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
}
