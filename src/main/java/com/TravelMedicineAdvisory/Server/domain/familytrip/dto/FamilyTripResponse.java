package com.TravelMedicineAdvisory.Server.domain.familytrip.dto;

import java.util.List;

public record FamilyTripResponse(
    Long id,
    String status,
    String destination,
    String country,
    Integer duration,
    String purpose,
    String tripType,
    String tripDetailsJson,
    Long baseFiatCost,
    Integer extraMemberCount,
    Long totalFiatCost,
    String currency,
    Long familyPlanId,
    List<FamilyTripMemberResponse> members
) {}
