package com.TravelMedicineAdvisory.Server.domain.familytrip.dto;

import java.util.List;

public record FamilyTripPreviewResponse(
    Integer includedMembers,
    Integer additionalMembers,
    Long baseFiatCost,
    Long extraFiatCost,
    Long totalFiatCost,
    String currency,
    Integer availableCredits,
    ActivePackageAllowanceDto activePackageAllowance,
    Boolean paymentRequired,
    List<PaymentBreakdownItem> paymentBreakdown
) {}
