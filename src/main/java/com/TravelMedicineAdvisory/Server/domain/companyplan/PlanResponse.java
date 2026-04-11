package com.TravelMedicineAdvisory.Server.domain.companyplan;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlanResponse(
        Long id,
        String code,
        String displayName,
        Integer signupCredits,
        Integer maxEmployees,
        Boolean customSupportEnabled,
        Boolean apiAccessEnabled,
        Boolean multipleAdminAccountsEnabled,
        Boolean highEmployeeLimitEnabled,
        BigDecimal priceUsd,
        BigDecimal priceNgn,
        BigDecimal priceEur,
        BigDecimal priceGbp,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
