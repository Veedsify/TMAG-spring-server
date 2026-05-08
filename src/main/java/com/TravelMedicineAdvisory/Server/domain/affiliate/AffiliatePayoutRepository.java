package com.TravelMedicineAdvisory.Server.domain.affiliate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AffiliatePayoutRepository extends JpaRepository<AffiliatePayout, Long> {
    List<AffiliatePayout> findByAffiliateProfileIdAndDeletedAtIsNullOrderByRequestedAtDesc(Long affiliateId);
}
