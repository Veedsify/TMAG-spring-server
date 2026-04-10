package com.TravelMedicineAdvisory.Server.domain.companyplan;

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
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
