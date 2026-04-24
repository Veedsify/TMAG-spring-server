package com.TravelMedicineAdvisory.Server.domain.companyadmin.credits;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.TravelMedicineAdvisory.Server.config.CallbackRegistry;
import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.TravelMedicineAdvisory.Server.security.AppUserDetails;

@Tag(name = "Company admin · Credits")
@RestController
@RequestMapping("/api/v1/company-admin/credits")
public class CompanyAdminCreditPurchaseController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyAdminCreditPurchaseController.class);

    private final CompanyAdminCreditPurchaseService service;
    private final UserRepository userRepository;
    private final CallbackRegistry callbackRegistry;

    public CompanyAdminCreditPurchaseController(
            CompanyAdminCreditPurchaseService service,
            UserRepository userRepository,
            CallbackRegistry callbackRegistry) {
        this.service = service;
        this.userRepository = userRepository;
        this.callbackRegistry = callbackRegistry;
    }

    @GetMapping("/pricing")
    public ResponseEntity<SuccessResponse> getCompanyPricing(@RequestParam Long companyId) {
        try {
            var pricing = service.getCompanyPricing(companyId);
            return ResponseEntity.ok(new SuccessResponse("Pricing fetched", pricing));
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/quote")
    public ResponseEntity<SuccessResponse> getQuote(
            @RequestParam Long companyId,
            @RequestParam Integer credits) {
        try {
            var quote = service.getQuote(companyId, credits);
            return ResponseEntity.ok(new SuccessResponse("Quote calculated", quote));
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse(e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/purchase")
    public ResponseEntity<SuccessResponse> initiatePurchase(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> body) {
        try {
            Long userId = getUserIdFromUserDetails(userDetails);

            if (body.get("companyId") == null || body.get("credits") == null) {
                return ResponseEntity.badRequest()
                        .body(new SuccessResponse("companyId and credits are required", null));
            }

            Long companyId = ((Number) body.get("companyId")).longValue();
            Integer credits = ((Number) body.get("credits")).intValue();

            var result = service.initiatePurchase(userId, companyId, credits, "ADMIN_CREDITS");

            return ResponseEntity.ok(new SuccessResponse("Payment initiated", Map.of(
                    "txRef", result.txRef(),
                    "paymentLink", result.paymentLink(),
                    "credits", result.credits(),
                    "amount", result.totalAmount(),
                    "currency", result.currency(),
                    "currencySymbol", result.currencySymbol(),
                    "purchaseId", result.purchaseId())));
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse(e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse(e.getMessage(), null));
        } catch (RuntimeException e) {
            logger.error("Payment initiation failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new SuccessResponse(e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Unexpected error during payment initiation: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new SuccessResponse("Payment initiation failed: " + e.getMessage(), null));
        }
    }

    @PostMapping("/purchase/hr")
    public ResponseEntity<SuccessResponse> initiateHrPurchase(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> body) {
        try {
            Long userId = getUserIdFromUserDetails(userDetails);

            if (body.get("companyId") == null || body.get("credits") == null) {
                return ResponseEntity.badRequest()
                        .body(new SuccessResponse("companyId and credits are required", null));
            }

            Long companyId = ((Number) body.get("companyId")).longValue();
            Integer credits = ((Number) body.get("credits")).intValue();

            var result = service.initiatePurchase(userId, companyId, credits, "HR_BILLING");

            return ResponseEntity.ok(new SuccessResponse("Payment initiated", Map.of(
                    "txRef", result.txRef(),
                    "paymentLink", result.paymentLink(),
                    "credits", result.credits(),
                    "amount", result.totalAmount(),
                    "currency", result.currency(),
                    "currencySymbol", result.currencySymbol(),
                    "purchaseId", result.purchaseId())));
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse(e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse(e.getMessage(), null));
        } catch (RuntimeException e) {
            logger.error("HR payment initiation failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new SuccessResponse(e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Unexpected error during HR payment initiation: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new SuccessResponse("Payment initiation failed: " + e.getMessage(), null));
        }
    }

    @GetMapping("/callback")
    public RedirectView paymentCallback(
            @RequestParam(required = false) String tx_ref,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String transaction_id,
            @RequestParam(required = false, defaultValue = "HR_BILLING") String type) {
        String frontendUrl = callbackRegistry.getFrontendRedirectUrl(type);

        if (tx_ref == null) {
            return new RedirectView(frontendUrl + "?error=Missing%20transaction%20reference");
        }

        try {
            var result = service.verifyAndCompletePurchase(tx_ref, transaction_id);

            if ("completed".equals(result.status())) {
                return new RedirectView(
                        frontendUrl +
                                "?success=true" +
                                "&credits=" + result.creditsPurchased() +
                                "&tx_ref=" + tx_ref);
            } else {
                String errorMsg = result.status() != null ? "Payment%20" + result.status()
                        : "Payment%20not%20completed";
                return new RedirectView(frontendUrl + "?success=false&error=" + errorMsg);
            }
        } catch (NoSuchElementException e) {
            return new RedirectView(frontendUrl + "?error=Transaction%20not%20found");
        } catch (Exception e) {
            return new RedirectView(frontendUrl + "?error=Payment%20verification%20failed");
        }
    }

    @GetMapping("/verify/{txRef}")
    public ResponseEntity<SuccessResponse> verifyPurchase(
            @PathVariable String txRef,
            @RequestParam(required = false) String transaction_id) {
        try {
            var result = service.verifyAndCompletePurchase(txRef, transaction_id);
            boolean isCompleted = "completed".equalsIgnoreCase(result.status());
            return ResponseEntity.ok(new SuccessResponse(
                    isCompleted ? "Payment verified" : "Payment " + result.status(),
                    Map.of("success", isCompleted, "purchase", result)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(new SuccessResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<SuccessResponse> getPurchaseHistory(
            @RequestParam(required = false) Long companyId) {
        var history = service.getPurchaseHistory(companyId);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", history));
    }

    @GetMapping("/{txRef}")
    public ResponseEntity<SuccessResponse> getPurchase(@PathVariable String txRef) {
        try {
            return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getPurchaseByTxRef(txRef)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(new SuccessResponse(e.getMessage(), null));
        }
    }

    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        if (userDetails instanceof AppUserDetails appUserDetails) {
            Long userId = appUserDetails.getUserId();
            if (userId != null) {
                return userId;
            }
        }

        String email = userDetails.getUsername();
        if (email != null) {
            return userRepository.findByEmail(email).map(u -> u.getId()).orElse(null);
        }

        throw new RuntimeException("Unable to extract user ID from authentication");
    }
}
