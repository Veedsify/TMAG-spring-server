package com.TravelMedicineAdvisory.Server.domain.planusageledger;

import java.time.LocalDateTime;

public record PlanUsageLedgerResponse(
    Long id,
    String action,
    String ipAddress,
    String userAgent,
    Long travelPlanId,
    Long userId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
