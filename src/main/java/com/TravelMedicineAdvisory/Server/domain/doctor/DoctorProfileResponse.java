package com.TravelMedicineAdvisory.Server.domain.doctor;

import java.time.LocalDateTime;

public record DoctorProfileResponse(
    Long userId,
    String firstName,
    String lastName,
    String email,
    String phone,
    String medicalLicenseNumber,
    String signatureUrl,
    String doctorApplicationStatus,
    LocalDateTime applicationSubmittedAt
) {}
