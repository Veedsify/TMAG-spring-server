package com.TravelMedicineAdvisory.Server.domain.doctor;

import java.time.LocalDateTime;

public record DoctorValidationDetailDto(
    Long planId,
    String destination,
    String country,
    String purpose,
    Integer duration,
    Integer riskScore,
    String validationStatus,
    LocalDateTime validatedAt,
    String validatedByName,
    String rejectionReason,
    String planTier,
    String travellerName,
    String travellerEmail,
    String travellerPhone,
    LocalDateTime createdAt,
    GeneratedPlanSnapshot generatedPlan,
    Object generatedPlanContent
) {}
