package com.TravelMedicineAdvisory.Server.domain.pricingplan;

import java.math.BigDecimal;

public record PricingPlanRequest(
    String name,
    BigDecimal price,
    String period,
    String description,
    String features,
    Integer creditsIncluded,
    Integer position,
    Boolean isActive
) {}
