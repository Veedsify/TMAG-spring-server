package com.TravelMedicineAdvisory.Server.domain.healthprofile;

import java.time.LocalDateTime;

public record HealthProfileResponse(
    Long id,
    String conditions,
    String medications,
    String allergies,
    String bloodType,
    String emergencyContactName,
    String emergencyContactPhone,
    Long userId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
