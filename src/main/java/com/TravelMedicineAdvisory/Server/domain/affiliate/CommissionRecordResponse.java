package com.TravelMedicineAdvisory.Server.domain.affiliate;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CommissionRecordResponse(
        Long id,
        @JsonProperty("affiliate_id") Long affiliateId,
        @JsonProperty("referral_link_id") Long referralLinkId,
        String campaign,
        BigDecimal amount,
        BigDecimal rate,
        String status,
        String currency,
        @JsonProperty("customer_email") String customerEmail,
        @JsonProperty("reference_type") String referenceType,
        @JsonProperty("reference_id") Long referenceId,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("paid_at") LocalDateTime paidAt
) {
    public static CommissionRecordResponse from(AffiliateCommission entity) {
        ReferralLink link = entity.getReferralLink();
        return new CommissionRecordResponse(
                entity.getId(),
                entity.getAffiliateProfile() != null ? entity.getAffiliateProfile().getId() : null,
                link != null ? link.getId() : null,
                link != null ? link.getCampaign() : "Referral",
                entity.getAmount(),
                entity.getRate(),
                entity.getStatus(),
                entity.getCurrency() != null ? entity.getCurrency() : "USD",
                entity.getCustomerEmail(),
                entity.getReferenceType(),
                entity.getReferenceId(),
                entity.getCreatedAt(),
                entity.getPaidAt()
        );
    }
}
