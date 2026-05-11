package com.TravelMedicineAdvisory.Server.domain.affiliate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AffiliateAuditLogRepository extends JpaRepository<AffiliateAuditLog, Long> {
    List<AffiliateAuditLog> findByAffiliateIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long affiliateId);
    List<AffiliateAuditLog> findByAdminUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long adminUserId);
    List<AffiliateAuditLog> findByDeletedAtIsNullOrderByCreatedAtDesc();
}
