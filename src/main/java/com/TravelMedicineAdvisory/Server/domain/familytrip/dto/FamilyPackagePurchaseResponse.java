package com.TravelMedicineAdvisory.Server.domain.familytrip.dto;

import java.time.LocalDateTime;

import com.TravelMedicineAdvisory.Server.domain.familytrip.FamilyPackagePurchase;

public record FamilyPackagePurchaseResponse(
        Long id,
        String txRef,
        String flwRef,
        Long userId,
        String packageType,
        Integer tripsAllowed,
        Integer tripsUsed,
        Integer additionalMembers,
        Integer totalMembers,
        Long amountPaidMinor,
        String currency,
        String paymentProvider,
        String paymentReference,
        String status,
        LocalDateTime expiresAt,
        LocalDateTime paidAt,
        String flutterwaveStatus,
        String failedReason,
        LocalDateTime createdAt
        ) {

    public static FamilyPackagePurchaseResponse from(FamilyPackagePurchase entity) {
        return new FamilyPackagePurchaseResponse(
                entity.getId(),
                entity.getTxRef(),
                entity.getFlwRef(),
                entity.getUser() != null ? entity.getUser().getId() : null,
                entity.getPackageType() != null ? entity.getPackageType().name() : null,
                entity.getTripsAllowed(),
                entity.getTripsUsed(),
                entity.getAdditionalMembers(),
                entity.getTotalMembers(),
                entity.getAmountPaidMinor(),
                entity.getCurrency(),
                entity.getPaymentProvider(),
                entity.getPaymentReference(),
                entity.getStatus() != null ? entity.getStatus().name() : null,
                entity.getExpiresAt(),
                entity.getPaidAt(),
                entity.getFlutterwaveStatus(),
                entity.getFailedReason(),
                entity.getCreatedAt()
        );
    }
}
