package com.TravelMedicineAdvisory.Server.domain.companyonboarding;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyOnboardingRepository extends JpaRepository<CompanyOnboardingEntity, Long> {
    List<CompanyOnboardingEntity> findByStatusOrderByCreatedAtDesc(OnboardingStatus status);
    java.util.Optional<CompanyOnboardingEntity> findByTxRef(String txRef);
}
