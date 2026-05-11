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
        Integer creditCount,
        String sampleRequest,
        List<TeamMemberResponse> teamMembers,
        List<PlatformEmployeeResponse> platformEmployees,
        String teamMembersCsvFileName,
        String teamMembersCsvUrl,
        String txRef,
        String paymentStatus,
        BigDecimal paymentAmount,
        BigDecimal baseAmount,
        BigDecimal discountAmount,
        String paymentCurrency,
        String status,
        String rejectionReason,
        String reviewedBy,
        LocalDateTime reviewedAt,
        Long createdCompanyId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public record TeamMemberResponse(
            String firstName,
            String lastName,
            String email,
            String role) {
    }

    public record PlatformEmployeeResponse(
            String email,
            String firstName,
            String lastName) {
    }
}
