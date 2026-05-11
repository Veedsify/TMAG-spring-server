package com.TravelMedicineAdvisory.Server.domain.affiliate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AffiliateProfileRepository extends JpaRepository<AffiliateProfile, Long> {
    Optional<AffiliateProfile> findByUserIdAndDeletedAtIsNull(Long userId);

    Optional<AffiliateProfile> findByReferralCodeAndDeletedAtIsNull(String referralCode);

    boolean existsByReferralCode(String referralCode);

    List<AffiliateProfile> findByDeletedAtIsNullOrderByCreatedAtDesc();

    Page<AffiliateProfile> findByDeletedAtIsNull(Pageable pageable);

    long countByStatusAndDeletedAtIsNull(String status);

    @Query("SELECT COALESCE(SUM(p.totalPaidOut), 0) FROM AffiliateProfile p WHERE p.deletedAt IS NULL")
    BigDecimal sumTotalPaidOut();

    @Query("SELECT COALESCE(SUM(p.pendingCommission), 0) FROM AffiliateProfile p WHERE p.deletedAt IS NULL")
    BigDecimal sumPendingCommission();

    @Query("SELECT p FROM AffiliateProfile p WHERE p.deletedAt IS NULL ORDER BY p.totalCommissionEarned DESC")
    List<AffiliateProfile> findTopByTotalCommissionEarned(Pageable pageable);

    Optional<AffiliateProfile> findByIdAndDeletedAtIsNull(Long id);
}
