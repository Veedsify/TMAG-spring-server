package com.TravelMedicineAdvisory.Server.domain.useronboarding;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserOnboardingRepository extends JpaRepository<UserOnboarding, Long> {
    Optional<UserOnboarding> findByUser_Email(String email);

    Optional<UserOnboarding> findByUser_Id(Long userId);

    void deleteByUser_Id(Long userId);
}
