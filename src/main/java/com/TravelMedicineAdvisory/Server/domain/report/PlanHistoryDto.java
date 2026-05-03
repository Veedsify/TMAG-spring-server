package com.TravelMedicineAdvisory.Server.domain.report;

public record PlanHistoryDto(
    Long planId,
    String destination,
    String country,
    String purpose,
    String tripType,
    String tripDetailsJson,
    Integer duration,
    Integer riskScore,
    String status,
    String employeeName,
    String medicalConsiderations,
    String vaccinations,
    String healthAlerts,
    String safetyAdvisories,
    String medications,
    String waterFood,
    String emergencyContacts,
    String generatedPlanStatus,
    String generatedPlanJson,
    String signedPdfUrl,
    String summaryPdfUrl,
    String createdAt,
    String updatedAt
) {}
