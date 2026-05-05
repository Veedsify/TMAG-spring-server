package com.TravelMedicineAdvisory.Server.domain.doctor;

public record DoctorReviewerDto(
        Long userId,
        String firstName,
        String lastName,
        String email,
        String profilePictureUrl,
        String bio) {
}
