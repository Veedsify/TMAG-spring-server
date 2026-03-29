package com.TravelMedicineAdvisory.Server.domain.useronboarding;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OnboardingQuestionCategoryRepository extends JpaRepository<OnboardingQuestionCategory, Long> {
    List<OnboardingQuestionCategory> findAllByOrderByDisplayOrderAsc();
    Optional<OnboardingQuestionCategory> findByCategoryKey(String categoryKey);
}
