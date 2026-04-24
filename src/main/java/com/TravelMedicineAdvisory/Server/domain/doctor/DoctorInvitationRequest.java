package com.TravelMedicineAdvisory.Server.domain.doctor;

public record DoctorInvitationRequest(
    String email,
    String firstName,
    String lastName
) {}
