package com.TravelMedicineAdvisory.Server.domain.doctor;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelPlanDoctorAssignmentRepository extends JpaRepository<TravelPlanDoctorAssignment, Long> {
    List<TravelPlanDoctorAssignment> findByTravelPlanIdAndDeletedAtIsNull(Long travelPlanId);
    boolean existsByTravelPlanIdAndDeletedAtIsNull(Long travelPlanId);
}
