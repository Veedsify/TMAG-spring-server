package com.TravelMedicineAdvisory.Server.domain.companyonboarding;

import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import com.TravelMedicineAdvisory.Server.config.CallbackRegistry;
import com.TravelMedicineAdvisory.Server.core.pricing.VolumePricingService;
import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Company Onboarding")
@RestController
@RequestMapping("/api/v1")
public class CompanyOnboardingController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyOnboardingController.class);

    private final CompanyOnboardingService service;
    private final CallbackRegistry callbackRegistry;
    private final VolumePricingService volumePricingService;

    public CompanyOnboardingController(CompanyOnboardingService service, CallbackRegistry callbackRegistry,
            VolumePricingService volumePricingService) {
        this.service = service;
        this.callbackRegistry = callbackRegistry;
        this.volumePricingService = volumePricingService;
    }

    // ============ PUBLIC ENDPOINTS ============

    @PostMapping(value = "/public/company-onboarding", consumes = { "multipart/form-data" })
    public ResponseEntity<SuccessResponse> submit(
            @RequestPart("request") CompanyOnboardingSubmitRequest request,
            @RequestPart(value = "teamMembersCsv", required = false) MultipartFile teamMembersCsv) {
        try {
            var response = service.submitOnboarding(request, teamMembersCsv);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new SuccessResponse("Onboarding request submitted", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/public/company-onboarding/{id}/pay")
    public ResponseEntity<SuccessResponse> initiatePayment(@PathVariable Long id) {
        try {
            var result = service.initiatePayment(id);
            return ResponseEntity.ok(new SuccessResponse("Payment initiated", result));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(new SuccessResponse(e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse(e.getMessage(), null));
        } catch (RuntimeException e) {
            logger.error("Payment initiation failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new SuccessResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/public/company-onboarding/verify")
    public ResponseEntity<SuccessResponse> verifyPayment(
            @RequestParam("tx_ref") String txRef,
            @RequestParam(required = false) String transaction_id) {
        try {
            var response = service.verifyPayment(txRef, transaction_id);
            boolean isApproved = "pending_approval".equalsIgnoreCase(response.status());
            return ResponseEntity.ok(new SuccessResponse(
                    isApproved ? "Payment verified successfully" : "Payment " + response.paymentStatus(),
                    response));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(new SuccessResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/public/company-onboarding/{id}/status")
    public ResponseEntity<SuccessResponse> getStatus(@PathVariable Long id) {
        try {
            var response = service.getStatus(id);
            return ResponseEntity.ok(new SuccessResponse("Fetched successfully", response));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(new SuccessResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/public/company-onboarding/pricing")
    public ResponseEntity<SuccessResponse> getPricingPreview(@RequestParam int credits) {
        var previews = volumePricingService.getPublicPricingPreviews(credits);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", previews));
    }

    @GetMapping("/public/company-onboarding/callback")
    public RedirectView onboardingPaymentCallback(
            @RequestParam(required = false) String tx_ref,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String transaction_id) {
        String frontendUrl = callbackRegistry.getFrontendRedirectUrl("COMPANY_ONBOARDING");

        if (tx_ref == null) {
            return new RedirectView(frontendUrl + "?error=Missing%20transaction%20reference");
        }

        try {
            service.verifyPayment(tx_ref, transaction_id);
            return new RedirectView(
                frontendUrl +
                "?tx_ref=" + tx_ref +
                "&transaction_id=" + (transaction_id != null ? transaction_id : "") +
                "&status=successful"
            );
        } catch (NoSuchElementException e) {
            return new RedirectView(
                frontendUrl +
                "?tx_ref=" + tx_ref +
                "&status=failed" +
                "&error=Transaction%20not%20found"
            );
        } catch (Exception e) {
            return new RedirectView(
                frontendUrl +
                "?tx_ref=" + tx_ref +
                "&status=failed" +
                "&error=Payment%20verification%20failed"
            );
        }
    }

    // ============ ADMIN ENDPOINTS ============

    @GetMapping("/admin/company-onboarding")
    @PreAuthorize("hasAuthority('all')")
    public ResponseEntity<SuccessResponse> listRequests(
            @RequestParam(required = false) String status) {
        OnboardingStatus filterStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                filterStatus = OnboardingStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        var responses = service.getRequestsByStatus(filterStatus);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", responses));
    }

    @GetMapping("/admin/company-onboarding/{id}")
    @PreAuthorize("hasAuthority('all')")
    public ResponseEntity<SuccessResponse> getRequest(@PathVariable Long id) {
        try {
            var response = service.getRequestById(id);
            return ResponseEntity.ok(new SuccessResponse("Fetched successfully", response));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(new SuccessResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/admin/company-onboarding/{id}/approve")
    @PreAuthorize("hasAuthority('all')")
    public ResponseEntity<SuccessResponse> approve(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String adminEmail = body != null ? body.getOrDefault("adminEmail", "admin") : "admin";
            var response = service.approveRequest(id, adminEmail);
            return ResponseEntity.ok(new SuccessResponse("Company registration approved", response));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(new SuccessResponse(e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/admin/company-onboarding/{id}/reject")
    @PreAuthorize("hasAuthority('all')")
    public ResponseEntity<SuccessResponse> reject(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            String reason = body.getOrDefault("reason", "");
            String adminEmail = body.getOrDefault("adminEmail", "admin");
            var response = service.rejectRequest(id, reason, adminEmail);
            return ResponseEntity.ok(new SuccessResponse("Company registration rejected", response));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(new SuccessResponse(e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse(e.getMessage(), null));
        }
    }
}
