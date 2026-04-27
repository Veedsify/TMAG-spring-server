package com.TravelMedicineAdvisory.Server.domain.admin.doctor;

public record AdminDoctorApplicationDto(
        Long userId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String licenseNumber,
        String specialization,
        String applicationStatus,
        String applicationSubmittedAt,
        String identityDocumentUrl,
        String licenseDocumentUrl,
        String createdAt) {
}
