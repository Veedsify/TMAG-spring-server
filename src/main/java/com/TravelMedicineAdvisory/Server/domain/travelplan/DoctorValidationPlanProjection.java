package com.TravelMedicineAdvisory.Server.domain.travelplan;

import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorValidationStatus;
import com.TravelMedicineAdvisory.Server.domain.plans.PlanTier;

import java.time.LocalDateTime;

public interface DoctorValidationPlanProjection {
    Long getPlanId();
    String getDestination();
    String getCountry();
    String getPurpose();
    Integer getDuration();
    Integer getRiskScore();
    DoctorValidationStatus getValidationStatus();
    PlanTier getPlanTier();
    String getTravellerFirstName();
    String getTravellerLastName();
    String getTravellerName();
    String getTravellerEmail();
    LocalDateTime getCreatedAt();
    String getGeneratedPlanStatus();
}
