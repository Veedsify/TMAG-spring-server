package com.TravelMedicineAdvisory.Server.domain.creditpricing;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

@Tag(name = "Credit pricing")
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
            java.util.Map<String, Object> response = service.buildCalculatePriceResponse(currency, credits);
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
