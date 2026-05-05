package com.TravelMedicineAdvisory.Server.domain.creditplan;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreditPlanResponse(
        Long id,
        String code,
        String displayName,
        BigDecimal basePriceUsd,
        BigDecimal basePriceNgn,
        String description,
        Boolean isDefault,
        Boolean isCompanyPlan,
        String signupRangeLabel,
        String serviceLevel,
        String visibility,
        Long assignedCompanyId,
        Integer planCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CreditPlanResponse from(CreditPlan entity) {
        return new CreditPlanResponse(
                entity.getId(),
                entity.getCode(),
                entity.getDisplayName(),
                entity.getBasePriceUsd(),
                entity.getBasePriceNgn(),
                entity.getDescription(),
                entity.getIsDefault(),
                entity.getIsCompanyPlan(),
                entity.getSignupRangeLabel(),
                entity.getServiceLevel(),
                entity.getVisibility(),
                entity.getAssignedCompanyId(),
                entity.getPlanCount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
