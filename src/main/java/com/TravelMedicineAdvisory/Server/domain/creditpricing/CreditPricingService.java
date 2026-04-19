package com.TravelMedicineAdvisory.Server.domain.creditpricing;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.core.cache.CacheNames;
import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;

@Service
@Transactional
public class CreditPricingService {

    private final CreditPricingRepository repository;

    public CreditPricingService(CreditPricingRepository repository) {
        this.repository = repository;
    }

    @Cacheable(cacheNames = CacheNames.CREDIT_PRICING)
    @Transactional(readOnly = true)
    public List<CreditPricingResponse> findAllActive() {
        return repository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(CreditPricingResponse::from)
                .toList();
    }

    @Cacheable(cacheNames = CacheNames.CREDIT_PRICING)
    @Transactional(readOnly = true)
    public List<CreditPricingResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(CreditPricingResponse::from)
                .toList();
    }

    @Cacheable(cacheNames = CacheNames.CREDIT_PRICING)
    @Transactional(readOnly = true)
    public CreditPricingResponse findById(Long id) {
        CreditPricing entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Credit pricing not found"));
        return CreditPricingResponse.from(entity);
    }

    @Cacheable(cacheNames = CacheNames.CREDIT_PRICING)
    @Transactional(readOnly = true)
    public CreditPricingResponse findByCurrency(BillingCurrency currency) {
        CreditPricing entity = repository.findByCurrency(currency)
                .orElseThrow(() -> new NoSuchElementException("Credit pricing not found for currency: " + currency));
        return CreditPricingResponse.from(entity);
    }

    /**
     * Single cached entry for {@code GET /credit-pricing/calculate}.
     */
    @Cacheable(cacheNames = CacheNames.CREDIT_PRICING)
    @Transactional(readOnly = true)
    public Map<String, Object> buildCalculatePriceResponse(BillingCurrency currency, int credits) {
        PriceCalculationResult result = calculatePriceWithDiscount(currency, credits);
        CreditPricingResponse pricing = findByCurrencyFromRepository(currency);
        Map<String, Object> response = new HashMap<>();
        response.put("currency", currency);
        response.put("currencySymbol", result.currencySymbol());
        response.put("credits", credits);
        response.put("pricePerCredit", pricing.pricePerCredit());
        response.put("basePrice", result.basePrice());
        response.put("discountAmount", result.discountAmount());
        response.put("totalPrice", result.totalPrice());
        response.put("appliedDiscountTier", result.appliedDiscountTier() != null ? result.appliedDiscountTier() : "NONE");
        response.put("discountTier1Threshold", pricing.discountTier1Threshold());
        response.put("discountTier1Amount", pricing.discountTier1Amount());
        response.put("discountTier2Threshold", pricing.discountTier2Threshold());
        response.put("discountTier2Amount", pricing.discountTier2Amount());
        response.put("discountTier3Threshold", pricing.discountTier3Threshold());
        response.put("discountTier3Amount", pricing.discountTier3Amount());
        return response;
    }

    private CreditPricingResponse findByCurrencyFromRepository(BillingCurrency currency) {
        CreditPricing entity = repository.findByCurrency(currency)
                .orElseThrow(() -> new NoSuchElementException("Credit pricing not found for currency: " + currency));
        return CreditPricingResponse.from(entity);
    }

    @CacheEvict(cacheNames = CacheNames.CREDIT_PRICING, allEntries = true)
    public CreditPricingResponse create(CreditPricingRequest request) {
        if (repository.existsByCurrency(request.currency())) {
            throw new IllegalStateException("Pricing already exists for currency: " + request.currency());
        }
        
        CreditPricing entity = new CreditPricing();
        mapRequestToEntity(request, entity);
        CreditPricing saved = repository.save(entity);
        return CreditPricingResponse.from(saved);
    }

    @CacheEvict(cacheNames = CacheNames.CREDIT_PRICING, allEntries = true)
    public CreditPricingResponse update(Long id, CreditPricingRequest request) {
        CreditPricing entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Credit pricing not found"));
        
        if (!entity.getCurrency().equals(request.currency()) && repository.existsByCurrency(request.currency())) {
            throw new IllegalStateException("Pricing already exists for currency: " + request.currency());
        }
        
        mapRequestToEntity(request, entity);
        CreditPricing saved = repository.save(entity);
        return CreditPricingResponse.from(saved);
    }

    @CacheEvict(cacheNames = CacheNames.CREDIT_PRICING, allEntries = true)
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Credit pricing not found");
        }
        repository.deleteById(id);
    }

    public record PriceCalculationResult(
        BigDecimal basePrice,
        BigDecimal discountAmount,
        BigDecimal totalPrice,
        String appliedDiscountTier,
        int credits,
        BillingCurrency currency,
        String currencySymbol
    ) {}

    public PriceCalculationResult calculatePriceWithDiscount(BillingCurrency currency, int credits) {
        CreditPricing pricing = repository.findByCurrency(currency)
                .orElseThrow(() -> new NoSuchElementException("Credit pricing not found for currency: " + currency));
        
        if (credits < pricing.getMinCredits() || credits > pricing.getMaxCredits()) {
            throw new IllegalArgumentException("Credits must be between " + pricing.getMinCredits() + " and " + pricing.getMaxCredits());
        }
        
        BigDecimal basePrice = pricing.getPricePerCredit().multiply(BigDecimal.valueOf(credits));
        BigDecimal discountAmount = pricing.getDiscountTier1Amount();
        String appliedTier = null;
        
        if (pricing.getDiscountTier3Threshold() != null && pricing.getDiscountTier3Amount() != null && 
            basePrice.compareTo(pricing.getDiscountTier3Threshold()) >= 0) {
            discountAmount = pricing.getDiscountTier3Amount();
            appliedTier = "TIER_3";
        } else if (pricing.getDiscountTier2Threshold() != null && pricing.getDiscountTier2Amount() != null && 
                   basePrice.compareTo(pricing.getDiscountTier2Threshold()) >= 0) {
            discountAmount = pricing.getDiscountTier2Amount();
            appliedTier = "TIER_2";
        } else if (pricing.getDiscountTier1Threshold() != null && pricing.getDiscountTier1Amount() != null && 
                   basePrice.compareTo(pricing.getDiscountTier1Threshold()) >= 0) {
            discountAmount = pricing.getDiscountTier1Amount();
            appliedTier = "TIER_1";
        } else {
            discountAmount = BigDecimal.ZERO;
        }
        
        BigDecimal totalPrice = basePrice.subtract(discountAmount);
        
        return new PriceCalculationResult(
            basePrice,
            discountAmount,
            totalPrice,
            appliedTier,
            credits,
            currency,
            pricing.getCurrencySymbol()
        );
    }

    public BigDecimal calculatePrice(BillingCurrency currency, int credits) {
        CreditPricing pricing = repository.findByCurrency(currency)
                .orElseThrow(() -> new NoSuchElementException("Credit pricing not found for currency: " + currency));
        
        if (credits < pricing.getMinCredits() || credits > pricing.getMaxCredits()) {
            throw new IllegalArgumentException("Credits must be between " + pricing.getMinCredits() + " and " + pricing.getMaxCredits());
        }
        
        return pricing.calculateTotalPrice(credits);
    }

    public CreditPricing getPricingEntity(BillingCurrency currency) {
        return repository.findByCurrency(currency)
                .orElseThrow(() -> new NoSuchElementException("Credit pricing not found for currency: " + currency));
    }

    private void mapRequestToEntity(CreditPricingRequest request, CreditPricing entity) {
        entity.setCurrency(request.currency());
        entity.setCurrencySymbol(request.currencySymbol());
        entity.setPricePerCredit(request.pricePerCredit());
        entity.setMinCredits(request.minCredits());
        entity.setMaxCredits(request.maxCredits());
        entity.setDiscountTier1Threshold(request.discountTier1Threshold());
        entity.setDiscountTier1Amount(request.discountTier1Amount());
        entity.setDiscountTier2Threshold(request.discountTier2Threshold());
        entity.setDiscountTier2Amount(request.discountTier2Amount());
        entity.setDiscountTier3Threshold(request.discountTier3Threshold());
        entity.setDiscountTier3Amount(request.discountTier3Amount());
        entity.setActive(request.isActive() != null ? request.isActive() : true);
        entity.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : 0);
    }
}
