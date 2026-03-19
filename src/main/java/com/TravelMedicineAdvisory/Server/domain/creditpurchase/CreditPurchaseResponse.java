package com.TravelMedicineAdvisory.Server.domain.creditpurchase;

import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreditPurchaseResponse(
    Long id,
    String txRef,
    String flwRef,
    Long userId,
    Integer creditsPurchased,
    BillingCurrency currency,
    String currencySymbol,
    BigDecimal pricePerCredit,
    BigDecimal amount,
    BigDecimal amountPaid,
    String status,
    String flutterwaveStatus,
    LocalDateTime paidAt,
    LocalDateTime failedAt,
    String failedReason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static CreditPurchaseResponse from(CreditPurchase entity) {
        return new CreditPurchaseResponse(
            entity.getId(),
            entity.getTxRef(),
            entity.getFlwRef(),
            entity.getUser() != null ? entity.getUser().getId() : null,
            entity.getCreditsPurchased(),
            entity.getCurrency(),
            entity.getCurrencySymbol(),
            entity.getPricePerCredit(),
            entity.getAmount(),
            entity.getAmountPaid(),
            entity.getStatus(),
            entity.getFlutterwaveStatus(),
            entity.getPaidAt(),
            entity.getFailedAt(),
            entity.getFailedReason(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
