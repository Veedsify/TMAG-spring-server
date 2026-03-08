package com.TravelMedicineAdvisory.Server.domain.companyuser;

public record CompanyUserRequest(
    String role,
    Long companyId,
    Long userId
) {}
