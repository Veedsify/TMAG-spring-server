package com.TravelMedicineAdvisory.Server.domain.affiliate;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

public record CreateReferralLinkRequest(
        @Size(max = 120, message = "Campaign must be 120 characters or less")
        String campaign,
        @JsonProperty("destination_url") String destinationUrl,
        @JsonProperty("credit_plan_id") Long creditPlanId,
        @JsonProperty("credit_plan_code") String creditPlanCode
) {}
