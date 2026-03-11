package com.TravelMedicineAdvisory.Server.domain.user;

import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateProfileRequest(
    @JsonProperty("first_name") String firstName,
    @JsonProperty("last_name") String lastName,
    String username,
    String phone,
    @JsonProperty("billing_currency") BillingCurrency billingCurrency
) {}
