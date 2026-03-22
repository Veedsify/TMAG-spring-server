package com.TravelMedicineAdvisory.Server.domain.companyuser;

import java.time.LocalDateTime;

public record CompanyUserResponse(
    Long id,
    String role,
    Integer creditsAllocated,
    Integer creditsUsed,
    Long companyId,
    Long userId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
