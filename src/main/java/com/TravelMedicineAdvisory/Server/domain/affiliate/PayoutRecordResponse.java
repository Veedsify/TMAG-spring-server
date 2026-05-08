package com.TravelMedicineAdvisory.Server.domain.affiliate;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PayoutRecordResponse(
        Long id,
        @JsonProperty("affiliate_id") Long affiliateId,
        BigDecimal amount,
        @JsonProperty("payment_method") String paymentMethod,
        @JsonProperty("payment_details") String paymentDetails,
        String status,
        String notes,
        @JsonProperty("requested_at") LocalDateTime requestedAt,
        @JsonProperty("processed_at") LocalDateTime processedAt
) {
    public static PayoutRecordResponse from(AffiliatePayout entity) {
        return new PayoutRecordResponse(
                entity.getId(),
                entity.getAffiliateProfile() != null ? entity.getAffiliateProfile().getId() : null,
                entity.getAmount(),
                entity.getPaymentMethod(),
                entity.getPaymentDetails(),
                entity.getStatus(),
                entity.getNotes(),
                entity.getRequestedAt(),
                entity.getProcessedAt()
        );
    }
}
