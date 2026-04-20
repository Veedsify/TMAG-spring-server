package com.TravelMedicineAdvisory.Server.domain.useronboarding;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OnboardingRequest(
        @JsonProperty("userType") String userType,
        @JsonProperty("nationality") String nationality,
        @JsonProperty("companyCode") String companyCode,
        @JsonProperty("complete") Boolean complete) {
}
