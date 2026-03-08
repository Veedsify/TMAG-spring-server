package com.TravelMedicineAdvisory.Server.domain.companyuser;

import java.time.LocalDateTime;

public record CompanyUserResponse(
    Long id,
    String role,
    Long companyId,
    Long userId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
