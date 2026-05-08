package com.TravelMedicineAdvisory.Server.domain.affiliate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AffiliateReferralRepository extends JpaRepository<AffiliateReferral, Long> {
    Optional<AffiliateReferral> findByReferredUserIdAndDeletedAtIsNull(Long referredUserId);

    boolean existsByReferredUserIdAndDeletedAtIsNull(Long referredUserId);
}
