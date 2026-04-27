package com.TravelMedicineAdvisory.Server.domain.admin.doctor;

public record AdminDoctorListItemDto(
        Long userId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String licenseNumber,
        String specialization,
        long validatedPlansCount,
        String createdAt) {
}
