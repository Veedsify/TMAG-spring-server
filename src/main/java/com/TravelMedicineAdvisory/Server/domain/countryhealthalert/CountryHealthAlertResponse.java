package com.TravelMedicineAdvisory.Server.domain.countryhealthalert;

import java.time.LocalDateTime;

public record CountryHealthAlertResponse(
    Long id,
    String title,
    String description,
    String severity,
    String alertType,
    String source,
    String sourceUrl,
    LocalDateTime startsAt,
    LocalDateTime expiresAt,
    Boolean isActive,
    Long countryId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
