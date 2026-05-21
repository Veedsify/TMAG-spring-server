package com.TravelMedicineAdvisory.Server.domain.companyadmin.management;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CompanyAdminCreditAllocationRequest(
        @NotNull(message = "companyId is required")
        Long companyId,

        @NotNull(message = "companyUserId is required")
        Long companyUserId,

        @NotNull(message = "creditsAllocated is required")
        @Min(value = 0, message = "creditsAllocated must be non-negative")
        Integer creditsAllocated) {
}
