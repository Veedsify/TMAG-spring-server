package com.TravelMedicineAdvisory.Server.domain.familytrip.dto;

import java.time.LocalDate;

public record FamilyTripMemberResponse(
    Long id,
    String relationship,
    String firstName,
    String lastName,
    String memberEmail,
    LocalDate dateOfBirth,
    Integer ageAtDeparture,
    Boolean includedInBase,
    String questionnaireStatus,
    Long travelPlanId,
    String loginCode
) {}
