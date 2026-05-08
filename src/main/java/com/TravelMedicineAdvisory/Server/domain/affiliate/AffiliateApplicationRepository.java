package com.TravelMedicineAdvisory.Server.domain.affiliate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AffiliateApplicationRepository extends JpaRepository<AffiliateApplication, Long> {
    List<AffiliateApplication> findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(String status);
    List<AffiliateApplication> findByDeletedAtIsNullOrderByCreatedAtDesc();
    boolean existsByEmailAndDeletedAtIsNull(String email);
    Optional<AffiliateApplication> findByIdAndDeletedAtIsNull(Long id);
    long countByStatusAndDeletedAtIsNull(String status);
}
