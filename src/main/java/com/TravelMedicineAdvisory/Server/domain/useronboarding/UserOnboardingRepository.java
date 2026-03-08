package com.TravelMedicineAdvisory.Server.domain.useronboarding;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserOnboardingRepository extends JpaRepository<UserOnboarding, Long> {
}
