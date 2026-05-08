package com.TravelMedicineAdvisory.Server.domain.affiliate;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public · Affiliate")
@RestController
@RequestMapping("/api/v1/public/affiliate")
public class PublicAffiliateController {

    private final AffiliateService affiliateService;

    public PublicAffiliateController(AffiliateService affiliateService) {
        this.affiliateService = affiliateService;
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

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }
}
