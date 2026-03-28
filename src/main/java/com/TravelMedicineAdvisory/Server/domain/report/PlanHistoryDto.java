package com.TravelMedicineAdvisory.Server.domain.report;

public record PlanHistoryDto(
    Long planId,
    String destination,
    String country,
    String purpose,
    Integer duration,
    Integer riskScore,
    String status,
    String employeeName,
    String createdAt,
    String updatedAt
) {}
