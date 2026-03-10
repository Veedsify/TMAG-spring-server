package com.TravelMedicineAdvisory.Server.domain.companyuser;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CompanyUserRequest(
        @JsonProperty("role") String role,
        @JsonProperty("company_id") Long company_id,
        @JsonProperty("user_id") Long user_id) {
}
