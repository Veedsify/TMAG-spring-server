package com.TravelMedicineAdvisory.Server.domain.user;

import java.time.LocalDateTime;

import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanResponse;

// user response for user controller
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
    String profilePictureOption,
    Integer credits,
    String type,
    Long roleId,
    BillingCurrency billingCurrency,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    CreditPlanResponse userCreditPlan
) {}
