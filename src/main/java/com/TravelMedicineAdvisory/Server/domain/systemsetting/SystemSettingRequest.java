package com.TravelMedicineAdvisory.Server.domain.systemsetting;

public record SystemSettingRequest(
    String key,
    String value,
    String type,
    String group,
    String label,
    String description,
    Boolean isPublic
) {}
