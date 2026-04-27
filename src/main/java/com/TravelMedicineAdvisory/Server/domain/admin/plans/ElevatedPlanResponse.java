package com.TravelMedicineAdvisory.Server.domain.admin.plans;

import java.time.LocalDateTime;

public record ElevatedPlanResponse(
        Long id,
        String destination,
        Integer duration,
        String purpose,
        Integer riskScore,
        String travellerName,
        String travellerEmail,
        String reviewDoctorName,
        String doctorFeedback,
        String pdfPreviewUrl,
        String summaryPreviewUrl,
        LocalDateTime elevatedAt) {
}
