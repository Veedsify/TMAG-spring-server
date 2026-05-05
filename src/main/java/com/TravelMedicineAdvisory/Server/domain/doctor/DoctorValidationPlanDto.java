package com.TravelMedicineAdvisory.Server.domain.doctor;

import java.time.LocalDateTime;
import java.util.List;

public record DoctorValidationPlanDto(
    Long planId,
    String destination,
    String country,
    String purpose,
    Integer duration,
    Integer riskScore,
    String validationStatus,
    String planTier,
    String travellerName,
    String travellerEmail,
    LocalDateTime createdAt,
    String generatedPlanStatus,
    List<AssignedDoctorDto> assignedDoctors,
    Boolean openToAllDoctors
) {}
