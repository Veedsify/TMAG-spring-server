package com.TravelMedicineAdvisory.Server.domain.admin.plans;

import java.time.LocalDateTime;
import java.util.List;

import com.TravelMedicineAdvisory.Server.domain.doctor.AssignedDoctorDto;

public record EscalatedPlanResponse(
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
        LocalDateTime escalatedAt,
        List<AssignedDoctorDto> assignedDoctors,
        Boolean openToAllDoctors) {
}
