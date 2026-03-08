package com.TravelMedicineAdvisory.Server.domain.pricingplan;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PricingPlanResponse(
    Long id,
    String name,
    BigDecimal price,
    String period,
    String description,
    String features,
    Integer creditsIncluded,
    Integer position,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
