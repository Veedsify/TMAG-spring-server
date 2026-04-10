package com.TravelMedicineAdvisory.Server.domain.company;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CompanyRequest(
    String name,
    String industry,
    Integer totalCredits,
    Integer usedCredits,
    Integer employeeCount,
    String plan,
    @JsonProperty("active_plan_id") Long activePlanId,
    String companyCode,
    Long logoId,
    @JsonProperty("billing_currency") BillingCurrency billingCurrency
) {}
