package com.TravelMedicineAdvisory.Server.domain.affiliate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AffiliateClickRepository extends JpaRepository<AffiliateClick, Long> {
    List<AffiliateClick> findByAffiliateProfileIdAndCreatedAtAfterAndDeletedAtIsNull(Long affiliateId, LocalDateTime createdAfter);
}
