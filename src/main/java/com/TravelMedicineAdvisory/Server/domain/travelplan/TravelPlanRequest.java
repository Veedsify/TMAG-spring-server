package com.TravelMedicineAdvisory.Server.domain.travelplan;

public record TravelPlanRequest(
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
    String questionnaireResponses,
    Long companyId,
    Long employeeId,
    Long userId
) {}
