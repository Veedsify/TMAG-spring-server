package com.TravelMedicineAdvisory.Server.domain.affiliate;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AffiliateProfileResponse(
        Long id,
        @JsonProperty("user_id") Long userId,
        @JsonProperty("referral_code") String referralCode,
        @JsonProperty("commission_rate") BigDecimal commissionRate,
        @JsonProperty("discount_rate") BigDecimal discountRate,
        @JsonProperty("total_clicks") Integer totalClicks,
        @JsonProperty("total_conversions") Integer totalConversions,
        @JsonProperty("total_commission_earned") BigDecimal totalCommissionEarned,
        @JsonProperty("total_paid_out") BigDecimal totalPaidOut,
        @JsonProperty("pending_commission") BigDecimal pendingCommission,
        String status,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("updated_at") LocalDateTime updatedAt
) {
    public static AffiliateProfileResponse from(AffiliateProfile entity) {
        return new AffiliateProfileResponse(
                entity.getId(),
                entity.getUser() != null ? entity.getUser().getId() : null,
                entity.getReferralCode(),
                entity.getCommissionRate(),
                entity.getDiscountRate(),
                entity.getTotalClicks(),
                entity.getTotalConversions(),
                entity.getTotalCommissionEarned(),
                entity.getTotalPaidOut(),
                entity.getPendingCommission(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
