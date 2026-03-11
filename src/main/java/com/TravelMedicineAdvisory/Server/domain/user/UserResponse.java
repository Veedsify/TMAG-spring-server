package com.TravelMedicineAdvisory.Server.domain.user;

import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
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
    BillingCurrency billingCurrency,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
