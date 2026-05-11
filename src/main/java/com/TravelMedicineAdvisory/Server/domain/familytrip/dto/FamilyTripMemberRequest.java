package com.TravelMedicineAdvisory.Server.domain.familytrip.dto;

import java.time.LocalDate;

public record FamilyTripMemberRequest(
    String relationship,
    String firstName,
    String lastName,
    String memberEmail,
    LocalDate dateOfBirth,
    String questionnaireResponses
) {}
