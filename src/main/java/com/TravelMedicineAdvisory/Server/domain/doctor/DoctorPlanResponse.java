package com.TravelMedicineAdvisory.Server.domain.doctor;

import java.time.LocalDateTime;

public record DoctorPlanResponse(
    Long id,
    String destination,
    String country,
    String userName,
    String userEmail,
    LocalDateTime createdAt,
    String planTier,
    String status,
    String doctorValidationStatus
) {}
