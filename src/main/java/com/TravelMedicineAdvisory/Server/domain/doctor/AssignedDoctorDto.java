package com.TravelMedicineAdvisory.Server.domain.doctor;

public record AssignedDoctorDto(
    Long doctorId,
    String firstName,
    String lastName,
    String email,
    String profilePictureUrl
) {}
