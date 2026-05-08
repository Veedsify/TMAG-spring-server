package com.TravelMedicineAdvisory.Server.domain.creditpurchase;

import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public record CreditPurchaseRequest(
    @NotNull(message = "Credits is required")
    @Min(value = 1, message = "Minimum credits is 1")
    @Max(value = 100, message = "Maximum credits is 100")
    Integer credits,
    
    @NotNull(message = "Currency is required")
    BillingCurrency currency,

    @JsonProperty("affiliate_referral_code")
    String affiliateReferralCode
) {}
