package com.TravelMedicineAdvisory.Server.domain.familytrip.dto;

public record PaymentBreakdownItem(
    String label,
    Long minor,
    String satisfiedBy
) {}
