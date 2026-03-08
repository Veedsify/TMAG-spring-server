package com.TravelMedicineAdvisory.Server.domain.healthprofile;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record HealthProfileResponse(
    Long id,
    String conditions,
    String medications,
    String allergies,
    @JsonProperty("blood_type") String bloodType,
    @JsonProperty("emergency_contact_name") String emergencyContactName,
    @JsonProperty("emergency_contact_phone") String emergencyContactPhone,
    @JsonProperty("user_id") Long userId,
    @JsonProperty("created_at") LocalDateTime createdAt,
    @JsonProperty("updated_at") LocalDateTime updatedAt
) {}
