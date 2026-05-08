package com.TravelMedicineAdvisory.Server.domain.affiliate;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.security.AppUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Affiliate")
@RestController
@RequestMapping("/api/v1/affiliate")
@PreAuthorize("hasAnyRole('SUPERADMIN','ADMINISTRATOR','AFFILIATE')")
public class AffiliateController {

    private final AffiliateService affiliateService;

    public AffiliateController(AffiliateService affiliateService) {
        this.affiliateService = affiliateService;
    }

    @GetMapping("/profile")
    public ResponseEntity<SuccessResponse> profile(@AuthenticationPrincipal AppUserDetails userDetails) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", affiliateService.getProfile(userDetails.getUserId())));
    }

    @GetMapping("/links")
    public ResponseEntity<SuccessResponse> links(@AuthenticationPrincipal AppUserDetails userDetails) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", affiliateService.getLinks(userDetails.getUserId())));
    }

    @PostMapping("/links")
    public ResponseEntity<SuccessResponse> createLink(
            @AuthenticationPrincipal AppUserDetails userDetails,
            @Valid @RequestBody CreateReferralLinkRequest request) {
        return ResponseEntity.ok(new SuccessResponse("Referral link created successfully", affiliateService.createReferralLink(userDetails.getUserId(), request)));
    }

    @GetMapping("/commissions")
    public ResponseEntity<SuccessResponse> commissions(@AuthenticationPrincipal AppUserDetails userDetails) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", affiliateService.getCommissions(userDetails.getUserId())));
    }

    @GetMapping("/payouts")
    public ResponseEntity<SuccessResponse> payouts(@AuthenticationPrincipal AppUserDetails userDetails) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", affiliateService.getPayouts(userDetails.getUserId())));
    }

    @PostMapping("/payouts")
    public ResponseEntity<SuccessResponse> requestPayout(
            @AuthenticationPrincipal AppUserDetails userDetails,
            @Valid @RequestBody PayoutRequest request) {
        return ResponseEntity.ok(new SuccessResponse("Payout requested successfully", affiliateService.requestPayout(userDetails.getUserId(), request)));
    }

    @GetMapping("/stats")
    public ResponseEntity<SuccessResponse> stats(@AuthenticationPrincipal AppUserDetails userDetails) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", affiliateService.getStats(userDetails.getUserId())));
    }
}
