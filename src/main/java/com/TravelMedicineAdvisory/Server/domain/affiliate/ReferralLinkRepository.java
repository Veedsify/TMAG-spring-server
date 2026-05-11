package com.TravelMedicineAdvisory.Server.domain.affiliate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReferralLinkRepository extends JpaRepository<ReferralLink, Long> {
    List<ReferralLink> findByAffiliateProfileIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long affiliateId);

    Optional<ReferralLink> findByShortCodeAndIsActiveTrueAndDeletedAtIsNull(String shortCode);

    boolean existsByShortCode(String shortCode);

    long countByAffiliateProfileIdAndIsActiveTrueAndDeletedAtIsNull(Long affiliateId);
}
