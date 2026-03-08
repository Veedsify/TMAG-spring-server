package com.TravelMedicineAdvisory.Server.domain.country;

import java.time.LocalDateTime;

public record CountryResponse(
    Long id,
    String name,
    String code,
    String region,
    String continent,
    String riskLevel,
    String visaInfo,
    String currency,
    String language,
    String timezone,
    String healthAdvisory,
    String travelAdvisory,
    String emergencyNumber,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
