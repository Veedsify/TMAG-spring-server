package com.TravelMedicineAdvisory.Server.domain.admin.doctor;

public record AdminDoctorListItemDto(
        Long userId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String licenseNumber,
        String specialization,
        String profilePictureUrl,
        String practicingLicenseUrl,
        String travelMedicineCertificateUrl,
        String bio,
        long validatedPlansCount,
        String createdAt) {
}
