package com.TravelMedicineAdvisory.Server.domain.admin.doctor;

public record AdminDoctorStatsDto(
        long totalDoctors,
        long pendingApplications,
        long approvedToday,
        long totalValidatedPlans) {
}
