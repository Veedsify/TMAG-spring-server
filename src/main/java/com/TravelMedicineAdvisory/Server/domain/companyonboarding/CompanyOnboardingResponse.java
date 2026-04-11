package com.TravelMedicineAdvisory.Server.domain.companyonboarding;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CompanyOnboardingResponse(
        Long id,
        String companyName,
        String industry,
        String contactEmail,
        String contactPhone,
        String website,
        String billingCurrency,
        String selectedPlanCode,
        String sampleRequest,
        List<TeamMemberResponse> teamMembers,
        String txRef,
        String paymentStatus,
        BigDecimal paymentAmount,
        String paymentCurrency,
        String status,
        String rejectionReason,
        String reviewedBy,
        LocalDateTime reviewedAt,
        Long createdCompanyId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public record TeamMemberResponse(
            String name,
            String email,
            String role) {
    }
}
