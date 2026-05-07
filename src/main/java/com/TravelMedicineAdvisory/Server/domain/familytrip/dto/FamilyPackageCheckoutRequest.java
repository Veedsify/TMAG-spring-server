package com.TravelMedicineAdvisory.Server.domain.familytrip.dto;

import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import com.TravelMedicineAdvisory.Server.domain.familytrip.FamilyPackageType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FamilyPackageCheckoutRequest(
        @NotNull(message = "Package type is required")
        FamilyPackageType packageType,
        BillingCurrency currency,
        @Min(value = 0, message = "Additional members cannot be negative")
        int additionalMembers,
        String name,
        String email,
        String phone
        ) {

}
