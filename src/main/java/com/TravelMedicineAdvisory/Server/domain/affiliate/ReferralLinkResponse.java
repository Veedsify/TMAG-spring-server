package com.TravelMedicineAdvisory.Server.domain.affiliate;

import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlan;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReferralLinkResponse(
        Long id,
        @JsonProperty("affiliate_id") Long affiliateId,
        String campaign,
        @JsonProperty("destination_url") String destinationUrl,
        @JsonProperty("short_code") String shortCode,
        Integer clicks,
        Integer conversions,
        @JsonProperty("commission_earned") BigDecimal commissionEarned,
        @JsonProperty("is_active") Boolean isActive,
        @JsonProperty("credit_plan_id") Long creditPlanId,
        @JsonProperty("credit_plan_code") String creditPlanCode,
        @JsonProperty("credit_plan_name") String creditPlanName,
        @JsonProperty("created_at") LocalDateTime createdAt
) {
    public static ReferralLinkResponse from(ReferralLink entity) {
        CreditPlan plan = entity.getCreditPlan();
        return new ReferralLinkResponse(
                entity.getId(),
                entity.getAffiliateProfile() != null ? entity.getAffiliateProfile().getId() : null,
                entity.getCampaign(),
                entity.getDestinationUrl(),
                entity.getShortCode(),
                entity.getClicks(),
                entity.getConversions(),
                entity.getCommissionEarned(),
                entity.getIsActive(),
                plan != null ? plan.getId() : null,
                plan != null ? plan.getCode() : null,
                plan != null ? plan.getDisplayName() : null,
                entity.getCreatedAt()
        );
    }
}
