package com.TravelMedicineAdvisory.Server.domain.creditpricing;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/credit-pricing")
public class CreditPricingController {

    private final CreditPricingService service;

    public CreditPricingController(CreditPricingService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<SuccessResponse> getActivePricing() {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findAllActive()));
    }

    @GetMapping("/all")
    public ResponseEntity<SuccessResponse> getAllPricing() {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findById(id)));
    }

    @GetMapping("/currency/{currency}")
    public ResponseEntity<SuccessResponse> getByCurrency(@PathVariable BillingCurrency currency) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findByCurrency(currency)));
    }

    @GetMapping("/calculate")
    public ResponseEntity<?> calculatePrice(
            @RequestParam BillingCurrency currency,
            @RequestParam int credits) {
        try {
            var result = service.calculatePriceWithDiscount(currency, credits);
            CreditPricingResponse pricing = service.findByCurrency(currency);
            java.util.Map<String, Object> response = new java.util.HashMap<>();
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
            return ResponseEntity.ok(new SuccessResponse("Calculated successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<SuccessResponse> create(@Valid @RequestBody CreditPricingRequest request) {
        return ResponseEntity.ok(new SuccessResponse("Created successfully", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CreditPricingRequest request) {
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
