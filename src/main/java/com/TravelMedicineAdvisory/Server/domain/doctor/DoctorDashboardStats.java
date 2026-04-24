package com.TravelMedicineAdvisory.Server.domain.doctor;

import java.util.List;

public record DoctorDashboardStats(
    long pendingValidations,
    long approvedToday,
    long totalValidated,
    List<DoctorValidationPlanDto> recentPlans
) {}
