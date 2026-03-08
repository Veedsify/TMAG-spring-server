package com.TravelMedicineAdvisory.Server.domain.user;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateProfileRequest(
    @JsonProperty("first_name") String firstName,
    @JsonProperty("last_name") String lastName,
    String username,
    String phone
) {}
