package com.TravelMedicineAdvisory.Server.domain.affiliate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AffiliateClickRepository extends JpaRepository<AffiliateClick, Long> {
    List<AffiliateClick> findByAffiliateProfileIdAndCreatedAtAfterAndDeletedAtIsNull(Long affiliateId, LocalDateTime createdAfter);

    List<AffiliateClick> findByAffiliateProfileIdAndCreatedAtBetweenAndDeletedAtIsNull(Long affiliateId, LocalDateTime start, LocalDateTime end);

    List<AffiliateClick> findByCreatedAtAfterAndDeletedAtIsNull(LocalDateTime createdAfter);

    long countByCreatedAtAfterAndDeletedAtIsNull(LocalDateTime createdAfter);

    long countByAffiliateProfileIdAndCreatedAtBetweenAndDeletedAtIsNull(Long affiliateId, LocalDateTime start, LocalDateTime end);
}
