package com.TravelMedicineAdvisory.Server.domain.useronboarding;

import java.time.LocalDateTime;

public record UserOnboardingResponse(
    Long id,
    String userType,
    String nationality,
    String companyCode,
    LocalDateTime completedAt,
    Long userId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Boolean questionnaireCompleted
) {}
