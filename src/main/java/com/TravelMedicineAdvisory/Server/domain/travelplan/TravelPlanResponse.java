package com.TravelMedicineAdvisory.Server.domain.travelplan;

import java.time.LocalDateTime;

public record TravelPlanResponse(
    Long id,
    String destination,
    String country,
    Integer duration,
    String purpose,
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
    LocalDateTime updatedAt
) {}
