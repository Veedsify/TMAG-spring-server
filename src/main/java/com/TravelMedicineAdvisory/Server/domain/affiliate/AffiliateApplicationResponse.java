package com.TravelMedicineAdvisory.Server.domain.affiliate;

import java.time.format.DateTimeFormatter;

public record AffiliateApplicationResponse(
        Long id,
        String fullName,
        String companyName,
        String email,
        String phone,
        String websiteUrl,
        String socialMediaLinks,
        String estimatedMonthlyReach,
        String promoDescription,
        Boolean agreedToTerms,
        String status,
        String rejectionReason,
        String adminNotes,
        String createdAt,
        String approvedAt
) {
    public static AffiliateApplicationResponse from(AffiliateApplication a) {
        return new AffiliateApplicationResponse(
                a.getId(),
                a.getFullName(),
                a.getCompanyName(),
                a.getEmail(),
                a.getPhone(),
                a.getWebsiteUrl(),
                a.getSocialMediaLinks(),
                a.getEstimatedMonthlyReach(),
                a.getPromoDescription(),
                a.getAgreedToTerms(),
                a.getStatus(),
                a.getRejectionReason(),
                a.getAdminNotes(),
                a.getCreatedAt() != null ? a.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null,
                a.getApprovedAt() != null ? a.getApprovedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null
        );
    }
}
