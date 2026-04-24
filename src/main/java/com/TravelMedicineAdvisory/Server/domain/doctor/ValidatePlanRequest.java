package com.TravelMedicineAdvisory.Server.domain.doctor;

public record ValidatePlanRequest(
    Long planId,
    boolean approved,
    String rejectionReason
) {}
