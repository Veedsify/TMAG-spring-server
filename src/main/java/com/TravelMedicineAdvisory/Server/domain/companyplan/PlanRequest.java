package com.TravelMedicineAdvisory.Server.domain.companyplan;

public record PlanRequest(
        PlanCode code,
        String displayName,
        Integer signupCredits,
        Integer maxEmployees,
        Boolean customSupportEnabled,
        Boolean apiAccessEnabled,
        Boolean multipleAdminAccountsEnabled,
        Boolean highEmployeeLimitEnabled) {
}
