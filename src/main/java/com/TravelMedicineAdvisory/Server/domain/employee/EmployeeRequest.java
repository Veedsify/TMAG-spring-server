package com.TravelMedicineAdvisory.Server.domain.employee;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EmployeeRequest(
    String name,
    String email,
    String department,
    Integer creditsUsed,
    Integer creditsAllocated,
    String status,
    Integer plansGenerated,
        @JsonProperty("company_id")
        Long companyId,
        @JsonProperty("user_id")
        Long userId
) {}
