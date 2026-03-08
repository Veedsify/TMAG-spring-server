package com.TravelMedicineAdvisory.Server.domain.healthprofile;

public record HealthProfileRequest(
    String conditions,
    String medications,
    String allergies,
    String bloodType,
    String emergencyContactName,
    String emergencyContactPhone,
    Long userId
) {}
