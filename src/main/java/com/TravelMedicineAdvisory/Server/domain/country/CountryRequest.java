package com.TravelMedicineAdvisory.Server.domain.country;

public record CountryRequest(
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
    Boolean isActive
) {}
