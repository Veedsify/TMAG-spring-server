package com.TravelMedicineAdvisory.Server.domain.report;

import java.time.LocalDateTime;

public record PlanHistoryProjection(
        Long planId,
        String destination,
        String country,
        String purpose,
        Integer duration,
        Integer riskScore,
        String status,
        String employeeName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
