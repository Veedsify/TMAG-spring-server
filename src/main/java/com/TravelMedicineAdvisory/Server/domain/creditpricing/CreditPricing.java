package com.TravelMedicineAdvisory.Server.domain.creditpricing;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;


import java.math.BigDecimal;

@Entity
@Table(name = "credit_pricing")
@SQLDelete(sql = "UPDATE credit_pricing SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")

public class CreditPricing extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private BillingCurrency currency;

    @Column(name = "currency_symbol", nullable = false)
    private String currencySymbol;

    @Column(name = "price_per_credit", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerCredit;

    @Column(name = "min_credits", nullable = false)
    private Integer minCredits = 1;

    @Column(name = "max_credits", nullable = false)
    private Integer maxCredits = 100;

    @Column(name = "discount_tier_1_threshold", precision = 12, scale = 2)
    private BigDecimal discountTier1Threshold;

    @Column(name = "discount_tier_1_amount", precision = 10, scale = 2)
    private BigDecimal discountTier1Amount;

    @Column(name = "discount_tier_2_threshold", precision = 12, scale = 2)
    private BigDecimal discountTier2Threshold;

    @Column(name = "discount_tier_2_amount", precision = 10, scale = 2)
    private BigDecimal discountTier2Amount;

    @Column(name = "discount_tier_3_threshold", precision = 12, scale = 2)
    private BigDecimal discountTier3Threshold;

    @Column(name = "discount_tier_3_amount", precision = 10, scale = 2)
    private BigDecimal discountTier3Amount;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    public BillingCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(BillingCurrency currency) {
        this.currency = currency;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public BigDecimal getPricePerCredit() {
        return pricePerCredit;
    }

    public void setPricePerCredit(BigDecimal pricePerCredit) {
        this.pricePerCredit = pricePerCredit;
    }

    public Integer getMinCredits() {
        return minCredits;
    }

    public void setMinCredits(Integer minCredits) {
        this.minCredits = minCredits;
    }

    public Integer getMaxCredits() {
        return maxCredits;
    }

    public void setMaxCredits(Integer maxCredits) {
        this.maxCredits = maxCredits;
    }

    public BigDecimal getDiscountTier1Threshold() {
        return discountTier1Threshold;
    }

    public void setDiscountTier1Threshold(BigDecimal discountTier1Threshold) {
        this.discountTier1Threshold = discountTier1Threshold;
    }

    public BigDecimal getDiscountTier1Amount() {
        return discountTier1Amount;
    }

    public void setDiscountTier1Amount(BigDecimal discountTier1Amount) {
        this.discountTier1Amount = discountTier1Amount;
    }

    public BigDecimal getDiscountTier2Threshold() {
        return discountTier2Threshold;
    }

    public void setDiscountTier2Threshold(BigDecimal discountTier2Threshold) {
        this.discountTier2Threshold = discountTier2Threshold;
    }

    public BigDecimal getDiscountTier2Amount() {
        return discountTier2Amount;
    }

    public void setDiscountTier2Amount(BigDecimal discountTier2Amount) {
        this.discountTier2Amount = discountTier2Amount;
    }

    public BigDecimal getDiscountTier3Threshold() {
        return discountTier3Threshold;
    }

    public void setDiscountTier3Threshold(BigDecimal discountTier3Threshold) {
        this.discountTier3Threshold = discountTier3Threshold;
    }

    public BigDecimal getDiscountTier3Amount() {
        return discountTier3Amount;
    }

    public void setDiscountTier3Amount(BigDecimal discountTier3Amount) {
        this.discountTier3Amount = discountTier3Amount;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public BigDecimal calculateTotalPrice(int credits) {
        BigDecimal basePrice = pricePerCredit.multiply(BigDecimal.valueOf(credits));
        BigDecimal totalDiscount = BigDecimal.ZERO;

        BigDecimal totalAmount = pricePerCredit.multiply(BigDecimal.valueOf(credits));

        if (discountTier3Threshold != null && discountTier3Amount != null && 
            totalAmount.compareTo(discountTier3Threshold) >= 0) {
            totalDiscount = discountTier3Amount;
        } else if (discountTier2Threshold != null && discountTier2Amount != null && 
                   totalAmount.compareTo(discountTier2Threshold) >= 0) {
            totalDiscount = discountTier2Amount;
        } else if (discountTier1Threshold != null && discountTier1Amount != null && 
                   totalAmount.compareTo(discountTier1Threshold) >= 0) {
            totalDiscount = discountTier1Amount;
        }

        return basePrice.subtract(totalDiscount);
    }

    public BigDecimal getDiscountForAmount(BigDecimal amount) {
        if (discountTier3Threshold != null && discountTier3Amount != null && 
            amount.compareTo(discountTier3Threshold) >= 0) {
            return discountTier3Amount;
        } else if (discountTier2Threshold != null && discountTier2Amount != null && 
                   amount.compareTo(discountTier2Threshold) >= 0) {
            return discountTier2Amount;
        } else if (discountTier1Threshold != null && discountTier1Amount != null && 
                   amount.compareTo(discountTier1Threshold) >= 0) {
            return discountTier1Amount;
        }
        return BigDecimal.ZERO;
    }
}
