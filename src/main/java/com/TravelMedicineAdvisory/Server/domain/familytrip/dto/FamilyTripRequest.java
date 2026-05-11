package com.TravelMedicineAdvisory.Server.domain.familytrip.dto;

import java.util.List;

public record FamilyTripRequest(
    String packageType,
    String destination,
    String country,
    Integer duration,
    String purpose,
    String tripType,
    String tripDetailsJson,
    List<FamilyTripMemberRequest> members
) {}
