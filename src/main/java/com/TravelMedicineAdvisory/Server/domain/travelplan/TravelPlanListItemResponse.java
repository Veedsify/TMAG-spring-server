package com.TravelMedicineAdvisory.Server.domain.travelplan;

import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorValidationStatus;
import com.TravelMedicineAdvisory.Server.domain.plans.PlanTier;

import java.time.LocalDateTime;

public record TravelPlanListItemResponse(
        Long id,
        String destination,
        String country,
        Integer duration,
        String purpose,
        Integer riskScore,
        String status,
        Long companyId,
        Long employeeId,
        Long userId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        PlanTier planTier,
        DoctorValidationStatus doctorValidationStatus,
        String validatedByName,
        LocalDateTime validatedAt,
        String rejectionReason,
        String signedPdfUrl,
        String summaryPdfUrl) {
}
