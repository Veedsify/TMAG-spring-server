package com.TravelMedicineAdvisory.Server.domain.affiliate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface AffiliateCommissionRepository extends JpaRepository<AffiliateCommission, Long> {
    List<AffiliateCommission> findByAffiliateProfileIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long affiliateId);

    boolean existsByReferenceTypeAndReferenceIdAndDeletedAtIsNull(String referenceType, Long referenceId);

    @Query("SELECT COALESCE(SUM(c.baseAmount), 0) FROM AffiliateCommission c WHERE c.deletedAt IS NULL")
    BigDecimal sumTotalRevenue();
}
