package com.TravelMedicineAdvisory.Server.domain.creditpricing;

import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreditPricingResponse(
    Long id,
    BillingCurrency currency,
    String currencySymbol,
    BigDecimal pricePerCredit,
    Integer minCredits,
    Integer maxCredits,
    BigDecimal discountTier1Threshold,
    BigDecimal discountTier1Amount,
    BigDecimal discountTier2Threshold,
    BigDecimal discountTier2Amount,
    BigDecimal discountTier3Threshold,
    BigDecimal discountTier3Amount,
    Boolean isActive,
    Integer displayOrder,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static CreditPricingResponse from(CreditPricing entity) {
        return new CreditPricingResponse(
            entity.getId(),
            entity.getCurrency(),
            entity.getCurrencySymbol(),
            entity.getPricePerCredit(),
            entity.getMinCredits(),
            entity.getMaxCredits(),
            entity.getDiscountTier1Threshold(),
            entity.getDiscountTier1Amount(),
            entity.getDiscountTier2Threshold(),
            entity.getDiscountTier2Amount(),
            entity.getDiscountTier3Threshold(),
            entity.getDiscountTier3Amount(),
            entity.getActive(),
            entity.getDisplayOrder(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public String getDiscountTierLabel(int credits, BigDecimal pricePerCredit) {
        BigDecimal total = pricePerCredit.multiply(BigDecimal.valueOf(credits));
        if (discountTier3Threshold != null && discountTier3Amount != null && 
            total.compareTo(discountTier3Threshold) >= 0) {
            return "Best Value!";
        } else if (discountTier2Threshold != null && discountTier2Amount != null && 
                   total.compareTo(discountTier2Threshold) >= 0) {
            return "Great Discount!";
        } else if (discountTier1Threshold != null && discountTier1Amount != null && 
                   total.compareTo(discountTier1Threshold) >= 0) {
            return "Bulk Discount!";
        }
        return null;
    }
}
