package com.TravelMedicineAdvisory.Server.domain.familytrip;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FamilyTripMemberQuestionnaireRepository extends JpaRepository<FamilyTripMemberQuestionnaire, Long> {
    List<FamilyTripMemberQuestionnaire> findByFamilyTripMemberId(Long memberId);
    List<FamilyTripMemberQuestionnaire> findByTravelPlanId(Long travelPlanId);
}
