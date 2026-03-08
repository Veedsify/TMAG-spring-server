package com.TravelMedicineAdvisory.Server.domain.countryaccommodation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CountryAccommodationRequest(
    String city,
    String type,
    String name,
    BigDecimal avgPricePerNight,
    String currency,
    Double rating,
    String sourceUrl,
    LocalDateTime lastUpdatedAt,
    Long countryId
) {}
