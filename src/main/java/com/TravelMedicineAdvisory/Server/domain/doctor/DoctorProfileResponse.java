package com.TravelMedicineAdvisory.Server.domain.doctor;

import java.time.LocalDateTime;

public record DoctorProfileResponse(
    Long userId,
    String firstName,
    String lastName,
    String email,
    String phone,
    String avatarUrl,
    String profilePictureOption,
    String medicalLicenseNumber,
    String signatureUrl,
    String stampUrl,
    String doctorApplicationStatus,
    LocalDateTime applicationSubmittedAt
) {}
