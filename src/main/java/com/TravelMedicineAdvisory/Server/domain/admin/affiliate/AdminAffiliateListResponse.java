package com.TravelMedicineAdvisory.Server.domain.admin.affiliate;

import com.TravelMedicineAdvisory.Server.domain.affiliate.AffiliateProfile;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public record AdminAffiliateListResponse(
        Long id,
        Long userId,
        String userName,
        String userEmail,
        String referralCode,
        BigDecimal commissionRate,
        BigDecimal discountRate,
        Integer totalClicks,
        Integer totalConversions,
        BigDecimal totalCommissionEarned,
        BigDecimal totalPaidOut,
        BigDecimal pendingCommission,
        String status,
        String createdAt
) {
    public static AdminAffiliateListResponse from(AffiliateProfile p) {
        String userName = null;
        String userEmail = null;
        Long userId = null;
        if (p.getUser() != null) {
            userId = p.getUser().getId();
            userName = p.getUser().getFirstName() != null
                    ? p.getUser().getFirstName() + (p.getUser().getLastName() != null ? " " + p.getUser().getLastName() : "")
                    : p.getUser().getEmail();
            userEmail = p.getUser().getEmail();
        }
        return new AdminAffiliateListResponse(
                p.getId(),
                userId,
                userName,
                userEmail,
                p.getReferralCode(),
                p.getCommissionRate(),
                p.getDiscountRate(),
                p.getTotalClicks() != null ? p.getTotalClicks() : 0,
                p.getTotalConversions() != null ? p.getTotalConversions() : 0,
                p.getTotalCommissionEarned() != null ? p.getTotalCommissionEarned() : BigDecimal.ZERO,
                p.getTotalPaidOut() != null ? p.getTotalPaidOut() : BigDecimal.ZERO,
                p.getPendingCommission() != null ? p.getPendingCommission() : BigDecimal.ZERO,
                p.getStatus(),
                p.getCreatedAt() != null ? p.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null
        );
    }
}
