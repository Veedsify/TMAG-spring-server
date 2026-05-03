package com.TravelMedicineAdvisory.Server.domain.creditplan;

import java.math.BigDecimal;

public record CustomCreditPlanRequest(
        String displayName,
        BigDecimal basePriceUsd,
        BigDecimal basePriceNgn,
        String description,
        String signupRangeLabel,
        String serviceLevel,
        Long assignedCompanyId,
        Integer planCount) {
}
