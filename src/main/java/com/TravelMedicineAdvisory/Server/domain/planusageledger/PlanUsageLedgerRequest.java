package com.TravelMedicineAdvisory.Server.domain.planusageledger;

public record PlanUsageLedgerRequest(
    String action,
    String ipAddress,
    String userAgent,
    Long travelPlanId,
    Long userId
) {}
