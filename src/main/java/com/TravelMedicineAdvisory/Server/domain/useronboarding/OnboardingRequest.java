package com.TravelMedicineAdvisory.Server.domain.useronboarding;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OnboardingRequest(
    @JsonProperty("user_type") String userType,
    String nationality,
    @JsonProperty("company_code") String companyCode
) {}
