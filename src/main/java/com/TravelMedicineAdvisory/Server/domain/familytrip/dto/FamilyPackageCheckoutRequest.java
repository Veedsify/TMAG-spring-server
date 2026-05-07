package com.TravelMedicineAdvisory.Server.domain.familytrip.dto;

import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import com.TravelMedicineAdvisory.Server.domain.familytrip.FamilyPackageType;

import jakarta.validation.constraints.NotNull;

public record FamilyPackageCheckoutRequest(
        @NotNull(message = "Package type is required")
        FamilyPackageType packageType,
        BillingCurrency currency,
        String name,
        String email,
        String phone
        ) {

}
