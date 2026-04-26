package com.TravelMedicineAdvisory.Server.domain.travelplan;

import java.time.LocalDateTime;

public record TravelPlanResponse(
    Long id,
    String destination,
    String country,
    Integer duration,
    String purpose,
    String tripType,
    String tripDetailsJson,
    Integer riskScore,
    String status,
    String medicalConsiderations,
    String vaccinations,
    String healthAlerts,
    String safetyAdvisories,
    String medications,
    String waterFood,
    String emergencyContacts,
    Long companyId,
    Long employeeId,
    Long userId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    GeneratedPlanPayload generatedPlan,
    String planTier,
    String doctorValidationStatus,
    String validatedByName,
    LocalDateTime validatedAt,
    String rejectionReason,
    String signedPdfUrl,
    String summaryPdfUrl
) {}
