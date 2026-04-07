package com.TravelMedicineAdvisory.Server.domain.travelplanquestionnaire;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelPlanQuestionnaireRepository extends JpaRepository<TravelPlanQuestionnaire, Long> {
    Optional<TravelPlanQuestionnaire> findByTravelPlan_Id(Long travelPlanId);
}
