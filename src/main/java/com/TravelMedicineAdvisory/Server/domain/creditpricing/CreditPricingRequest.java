package com.TravelMedicineAdvisory.Server.domain.creditpricing;

import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreditPricingRequest(
    @NotNull(message = "Currency is required")
    BillingCurrency currency,
    
    @NotBlank(message = "Currency symbol is required")
    String currencySymbol,
    
    @NotNull(message = "Price per credit is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    BigDecimal pricePerCredit,
    
    @NotNull(message = "Minimum credits is required")
    @Min(value = 1, message = "Minimum credits must be at least 1")
    Integer minCredits,
    
    @NotNull(message = "Maximum credits is required")
    @Min(value = 1, message = "Maximum credits must be at least 1")
    Integer maxCredits,
    
    BigDecimal discountTier1Threshold,
    BigDecimal discountTier1Amount,
    BigDecimal discountTier2Threshold,
    BigDecimal discountTier2Amount,
    BigDecimal discountTier3Threshold,
    BigDecimal discountTier3Amount,
    
    Boolean isActive,
    
    Integer displayOrder
) {}
