package com.TravelMedicineAdvisory.Server.domain.affiliate;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record AffiliateDiscountResponse(
        boolean active,
        @JsonProperty("short_code") String shortCode,
        @JsonProperty("referral_code") String referralCode,
        @JsonProperty("discount_rate") BigDecimal discountRate
) {}
