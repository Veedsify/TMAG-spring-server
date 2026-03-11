package com.TravelMedicineAdvisory.Server.domain.user;

import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;

public record UserRequest(
    String firstName,
    String lastName,
    String name,
    String username,
    String phone,
    String email,
    Integer onboardingStage,
    Boolean onboarded,
    Boolean isVerified,
    String avatarUrl,
    Integer credits,
    String type,
    Long roleId,
    BillingCurrency billingCurrency
) {}
