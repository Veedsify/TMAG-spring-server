package com.TravelMedicineAdvisory.Server.domain.affiliate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AffiliateCommissionRepository extends JpaRepository<AffiliateCommission, Long> {
    List<AffiliateCommission> findByAffiliateProfileIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long affiliateId);

    boolean existsByReferenceTypeAndReferenceIdAndDeletedAtIsNull(String referenceType, Long referenceId);
}
