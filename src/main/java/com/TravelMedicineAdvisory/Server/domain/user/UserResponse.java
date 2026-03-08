package com.TravelMedicineAdvisory.Server.domain.user;

import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String firstName,
    String lastName,
    String name,
    String username,
    String phone,
    String email,
    Integer onboardingStage,
    Boolean onboarded,
    Boolean isVerified,
    LocalDateTime lastLogin,
    String avatarUrl,
    Integer credits,
    String type,
    Long roleId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
