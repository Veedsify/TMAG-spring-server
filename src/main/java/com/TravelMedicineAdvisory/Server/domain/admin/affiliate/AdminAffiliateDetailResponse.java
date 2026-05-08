package com.TravelMedicineAdvisory.Server.domain.admin.affiliate;

import com.TravelMedicineAdvisory.Server.domain.affiliate.AffiliateCommission;
import com.TravelMedicineAdvisory.Server.domain.affiliate.AffiliatePayout;
import com.TravelMedicineAdvisory.Server.domain.affiliate.AffiliateProfile;
import com.TravelMedicineAdvisory.Server.domain.affiliate.AffiliateReferral;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record AdminAffiliateDetailResponse(
        Long id,
        Long userId,
        String userName,
        String userEmail,
        String userPhone,
        String referralCode,
        BigDecimal commissionRate,
        BigDecimal discountRate,
        Integer totalClicks,
        Integer totalConversions,
        BigDecimal totalCommissionEarned,
        BigDecimal totalPaidOut,
        BigDecimal pendingCommission,
        String status,
        String createdAt,
        List<CommissionItem> commissions,
        List<PayoutItem> payouts,
        List<AdminAffiliateReferralResponse> referrals
) {
    public record CommissionItem(
            Long id,
            BigDecimal amount,
            BigDecimal baseAmount,
            BigDecimal rate,
            String status,
            String customerEmail,
            String referenceType,
            Long referenceId,
            String createdAt
    ) {
        public static CommissionItem from(AffiliateCommission c) {
            return new CommissionItem(
                    c.getId(),
                    c.getAmount(),
                    c.getBaseAmount(),
                    c.getRate(),
                    c.getStatus(),
                    c.getCustomerEmail(),
                    c.getReferenceType(),
                    c.getReferenceId(),
                    c.getCreatedAt() != null ? c.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null
            );
        }
    }

    public record PayoutItem(
            Long id,
            BigDecimal amount,
            String paymentMethod,
            String status,
            String requestedAt,
            String processedAt
    ) {
        public static PayoutItem from(AffiliatePayout p) {
            return new PayoutItem(
                    p.getId(),
                    p.getAmount(),
                    p.getPaymentMethod(),
                    p.getStatus(),
                    p.getRequestedAt() != null ? p.getRequestedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null,
                    p.getProcessedAt() != null ? p.getProcessedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null
            );
        }
    }

    public static AdminAffiliateDetailResponse from(
            AffiliateProfile p,
            List<AffiliateCommission> commissions,
            List<AffiliatePayout> payouts,
            List<AffiliateReferral> referrals) {

        String userName = null;
        String userEmail = null;
        String userPhone = null;
        Long userId = null;
        if (p.getUser() != null) {
            userId = p.getUser().getId();
            userName = p.getUser().getFirstName() != null
                    ? p.getUser().getFirstName() + (p.getUser().getLastName() != null ? " " + p.getUser().getLastName() : "")
                    : p.getUser().getEmail();
            userEmail = p.getUser().getEmail();
            userPhone = p.getUser().getPhone();
        }

        return new AdminAffiliateDetailResponse(
                p.getId(),
                userId,
                userName,
                userEmail,
                userPhone,
                p.getReferralCode(),
                p.getCommissionRate(),
                p.getDiscountRate(),
                p.getTotalClicks() != null ? p.getTotalClicks() : 0,
                p.getTotalConversions() != null ? p.getTotalConversions() : 0,
                p.getTotalCommissionEarned() != null ? p.getTotalCommissionEarned() : BigDecimal.ZERO,
                p.getTotalPaidOut() != null ? p.getTotalPaidOut() : BigDecimal.ZERO,
                p.getPendingCommission() != null ? p.getPendingCommission() : BigDecimal.ZERO,
                p.getStatus(),
                p.getCreatedAt() != null ? p.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null,
                commissions.stream().map(CommissionItem::from).toList(),
                payouts.stream().map(PayoutItem::from).toList(),
                referrals.stream().map(AdminAffiliateReferralResponse::from).toList()
        );
    }
}
