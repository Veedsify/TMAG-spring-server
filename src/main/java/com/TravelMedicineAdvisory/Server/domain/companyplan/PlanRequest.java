package com.TravelMedicineAdvisory.Server.domain.companyplan;

import java.math.BigDecimal;

public record PlanRequest(
        PlanCode code,
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
        BigDecimal priceGbp) {
}
