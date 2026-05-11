package com.TravelMedicineAdvisory.Server.domain.affiliate;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record AffiliateTrackingResponse(
        @JsonProperty("short_code") String shortCode,
        @JsonProperty("referral_code") String referralCode,
        @JsonProperty("destination_url") String destinationUrl,
        @JsonProperty("discount_rate") BigDecimal discountRate,
        @JsonProperty("cookie_days") Integer cookieDays
) {}
