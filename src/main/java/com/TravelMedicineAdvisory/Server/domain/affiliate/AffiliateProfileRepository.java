package com.TravelMedicineAdvisory.Server.domain.affiliate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AffiliateProfileRepository extends JpaRepository<AffiliateProfile, Long> {
    Optional<AffiliateProfile> findByUserIdAndDeletedAtIsNull(Long userId);

    Optional<AffiliateProfile> findByReferralCodeAndDeletedAtIsNull(String referralCode);

    boolean existsByReferralCode(String referralCode);
}
