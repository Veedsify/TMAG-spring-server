package com.TravelMedicineAdvisory.Server.domain.user;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProfileResponse(
        Long id,
        String email,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        String username,
        String phone,
        String name,
        @JsonProperty("avatar_url") String avatarUrl,
        String type,
        Boolean onboarded,
        @JsonProperty("onboarding_stage") Integer onboardingStage,
        Integer credits,
        Boolean isVerified,
        @JsonProperty("role_id") Long roleId) {
}
