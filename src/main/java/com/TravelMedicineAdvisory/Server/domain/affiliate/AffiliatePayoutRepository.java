package com.TravelMedicineAdvisory.Server.domain.affiliate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AffiliatePayoutRepository extends JpaRepository<AffiliatePayout, Long> {
    List<AffiliatePayout> findByAffiliateProfileIdAndDeletedAtIsNullOrderByRequestedAtDesc(Long affiliateId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM AffiliatePayout p WHERE p.deletedAt IS NULL AND p.createdAt >= :since")
    BigDecimal sumAmountByCreatedAtAfter(LocalDateTime since);
}
