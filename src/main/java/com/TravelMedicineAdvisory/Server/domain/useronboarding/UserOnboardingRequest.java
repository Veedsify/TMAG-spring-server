package com.TravelMedicineAdvisory.Server.domain.useronboarding;

import java.time.LocalDateTime;

public record UserOnboardingRequest(
    String userType,
    String nationality,
    String companyCode,
    LocalDateTime completedAt,
    Long userId
) {}
