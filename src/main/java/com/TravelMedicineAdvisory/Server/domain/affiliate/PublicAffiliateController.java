package com.TravelMedicineAdvisory.Server.domain.affiliate;

import com.TravelMedicineAdvisory.Server.core.payment.FlutterwaveService;
import com.TravelMedicineAdvisory.Server.core.payment.PaystackService;
import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public · Affiliate")
@RestController
@RequestMapping("/api/v1/public/affiliate")
public class PublicAffiliateController {

    private final AffiliateService affiliateService;
    private final FlutterwaveService flutterwaveService;
    private final PaystackService paystackService;

    public PublicAffiliateController(AffiliateService affiliateService, FlutterwaveService flutterwaveService, PaystackService paystackService) {
        this.affiliateService = affiliateService;
        this.flutterwaveService = flutterwaveService;
        this.paystackService = paystackService;
    }

    @GetMapping("/track/{shortCode}")
    public ResponseEntity<SuccessResponse> track(
            @PathVariable String shortCode,
            HttpServletRequest request) {
        String ipAddress = firstNonBlank(request.getHeader("X-Forwarded-For"), request.getRemoteAddr());
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        String userAgent = request.getHeader("User-Agent");
        return ResponseEntity.ok(new SuccessResponse("Referral tracked successfully", affiliateService.trackClick(shortCode, ipAddress, userAgent)));
    }

    @GetMapping("/discount/{referralCode}")
    public ResponseEntity<SuccessResponse> discount(@PathVariable String referralCode) {
        AffiliateDiscountResponse response = affiliateService.getDiscount(referralCode)
                .orElse(new AffiliateDiscountResponse(false, null, null, java.math.BigDecimal.ZERO));
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", response));
    }

    @PostMapping("/apply")
    public ResponseEntity<SuccessResponse> apply(@Valid @RequestBody AffiliateApplicationRequest request) {
        return ResponseEntity.ok(new SuccessResponse("Application submitted successfully", affiliateService.submitApplication(request)));
    }

    @GetMapping("/banks/{country}")
    public ResponseEntity<SuccessResponse> getBanks(@PathVariable String country) {
        // Paystack uses standard CBN NUBAN codes that are consistent with their resolve endpoint.
        // Fall back to Flutterwave if Paystack is not configured.
        Object banks = "NG".equalsIgnoreCase(country) && paystackService.isConfigured()
                ? paystackService.getBanks()
                : flutterwaveService.getBanks(country);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", banks));
    }

    @PostMapping("/banks/validate")
    public ResponseEntity<SuccessResponse> validateAccount(@RequestBody java.util.Map<String, String> body) {
        String accountNumber = body.get("accountNumber");
        String bankCode = body.get("bankCode");
        // Prefer Paystack for NGN account resolution — reliable test mode and standard bank codes.
        java.util.Map<String, String> result = paystackService.isConfigured()
                ? paystackService.resolveAccount(accountNumber, bankCode)
                : flutterwaveService.resolveAccount(accountNumber, bankCode);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", result));
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }
}
