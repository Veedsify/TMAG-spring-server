package com.TravelMedicineAdvisory.Server.domain.employee;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EmployeeInviteRequest(
    String name,
    String email,
    String department,
    String role,
    @JsonProperty("creditsAllocated")
    Integer creditsAllocated,
    @JsonProperty("companyId")
    Long companyId
) {}
