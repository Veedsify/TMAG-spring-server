package com.TravelMedicineAdvisory.Server.domain.company;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CompanyRequest(
    String name,
    String industry,
    Integer totalCredits,
    Integer usedCredits,
    Integer employeeCount,
    String plan,
    String companyCode,
    Long logoId,
    @JsonProperty("billing_currency") BillingCurrency billingCurrency
) {}
