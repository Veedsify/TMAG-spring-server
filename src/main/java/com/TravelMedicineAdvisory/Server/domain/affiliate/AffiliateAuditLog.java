package com.TravelMedicineAdvisory.Server.domain.affiliate;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "affiliate_audit_logs", indexes = {
        @Index(name = "idx_affiliate_audit_affiliate", columnList = "affiliate_id"),
        @Index(name = "idx_affiliate_audit_admin", columnList = "admin_user_id"),
        @Index(name = "idx_affiliate_audit_action", columnList = "action"),
        @Index(name = "idx_affiliate_audit_created", columnList = "created_at")
})
public class AffiliateAuditLog extends BaseEntity {

    @Column(name = "affiliate_id", nullable = false)
    private Long affiliateId;

    @Column(name = "admin_user_id", nullable = false)
    private Long adminUserId;

    @Column(name = "admin_email")
    private String adminEmail;

    @Column(nullable = false, length = 40)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String details;

    public AffiliateAuditLog() {}

    public AffiliateAuditLog(Long affiliateId, Long adminUserId, String adminEmail, String action, String description, String details) {
        this.affiliateId = affiliateId;
        this.adminUserId = adminUserId;
        this.adminEmail = adminEmail;
        this.action = action;
        this.description = description;
        this.details = details;
    }

    public Long getAffiliateId() {
        return affiliateId;
    }

    public void setAffiliateId(Long affiliateId) {
        this.affiliateId = affiliateId;
    }

    public Long getAdminUserId() {
        return adminUserId;
    }

    public void setAdminUserId(Long adminUserId) {
        this.adminUserId = adminUserId;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
