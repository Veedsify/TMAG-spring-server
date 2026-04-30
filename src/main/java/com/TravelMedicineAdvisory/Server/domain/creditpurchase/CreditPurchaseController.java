package com.TravelMedicineAdvisory.Server.domain.creditpurchase;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.TravelMedicineAdvisory.Server.config.CallbackRegistry;
import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.TravelMedicineAdvisory.Server.security.AppUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Tag(name = "Credit purchases")
@RestController
@RequestMapping("/api/v1/credit-purchases")
public class CreditPurchaseController {

    private final CreditPurchaseService service;
    private final UserRepository userRepository;
    private final CallbackRegistry callbackRegistry;

    public CreditPurchaseController(CreditPurchaseService service, UserRepository userRepository, CallbackRegistry callbackRegistry) {
        this.service = service;
        this.userRepository = userRepository;
        this.callbackRegistry = callbackRegistry;
    }

    @PostMapping("/initiate")
    @PreAuthorize("@perm.has(authentication, 'credit:create')")
    public ResponseEntity<SuccessResponse> initiatePurchase(
            @AuthenticationPrincipal AppUserDetails userDetails,
            @Valid @RequestBody CreditPurchaseRequest request) {
        try {
            Long userId = userDetails.getUserId();
            var result = service.initiatePurchase(userId, request);
            Map<String, Object> responseData = new java.util.HashMap<>();
            responseData.put("success", true);
            responseData.put("txRef", result.txRef());
            responseData.put("paymentLink", result.paymentLink());
            responseData.put("credits", result.credits());
            responseData.put("basePrice", result.basePrice());
            responseData.put("discountAmount", result.discountAmount());
            responseData.put("amount", result.totalAmount());
            responseData.put("currency", result.currency());
            responseData.put("currencySymbol", result.currencySymbol());
            responseData.put("pricePerCredit", result.pricePerCredit());
            responseData.put("purchaseId", result.purchaseId());
            return ResponseEntity.ok(new SuccessResponse("Payment initiated successfully", responseData));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse(e.getMessage(), Map.of("success", false, "errorType", "COMPANY_USER_ERROR")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse(e.getMessage(), Map.of("success", false, "errorType", "VALIDATION_ERROR")));
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse(e.getMessage(), Map.of("success", false, "errorType", "NOT_FOUND")));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse(e.getMessage(), Map.of("success", false, "errorType", "PAYMENT_ERROR")));
        }
    }

    @GetMapping("/verify/{txRef}")
    @PreAuthorize("@perm.has(authentication, 'credit:read')")
    public ResponseEntity<SuccessResponse> verifyPurchase(
            @PathVariable String txRef,
            @RequestParam(required = false) String transaction_id) {
        try {
            var result = service.verifyAndCompletePurchase(txRef, transaction_id);
            boolean isCompleted = "completed".equalsIgnoreCase(result.status());
            String message = isCompleted ? "Payment verified successfully" : "Payment " + (result.status() != null ? result.status() : "not completed");
            return ResponseEntity.ok(new SuccessResponse(message, Map.of("success", isCompleted, "purchase", result)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/callback")
    public RedirectView paymentCallback(
            @RequestParam(required = false) String tx_ref,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String transaction_id) {
        String frontendUrl = callbackRegistry.getFrontendRedirectUrl("CREDIT_PURCHASE");

        if (tx_ref == null) {
            return new RedirectView(frontendUrl + "?error=Missing%20transaction%20reference");
        }

        try {
            var result = service.verifyAndCompletePurchase(tx_ref, transaction_id);

            if ("completed".equals(result.status())) {
                // Determine redirect destination based on user's onboarding status
                Optional<User> user = userRepository.findById(result.userId());
                String redirectTo = "onboarding"; // default

                if (user.isPresent()) {
                    User u = user.get();
                    boolean isOnboarded = Boolean.TRUE.equals(u.getOnboarded());
                    redirectTo = isOnboarded ? "settings" : "onboarding";

                    // If not yet onboarded, advance stage to 5 and mark as onboarded
                    if (!isOnboarded) {
                        u.setOnboardingStage(5);
                        u.setOnboarded(true);
                        userRepository.save(u);
                    }
                }

                return new RedirectView(
                    frontendUrl +
                    "?success=true" +
                    "&credits=" + result.creditsPurchased() +
                    "&tx_ref=" + tx_ref +
                    "&redirect=" + redirectTo
                );
            } else {
                String errorMsg = result.status() != null ? "Payment%20" + result.status() : "Payment%20not%20completed";
                Optional<User> user = userRepository.findById(result.userId());
                String redirectTo = user.map(u -> Boolean.TRUE.equals(u.getOnboarded()) ? "settings" : "onboarding").orElse("onboarding");
                return new RedirectView(frontendUrl + "?success=false&error=" + errorMsg + "&redirect=" + redirectTo);
            }
        } catch (NoSuchElementException e) {
            return new RedirectView(frontendUrl + "?error=Transaction%20not%20found&redirect=onboarding");
        } catch (Exception e) {
            return new RedirectView(frontendUrl + "?error=Payment%20verification%20failed&redirect=onboarding");
        }
    }

    @GetMapping("/history")
    @PreAuthorize("@perm.has(authentication, 'credit:read')")
    public ResponseEntity<SuccessResponse> getPurchaseHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getPurchaseHistory(userId)));
    }

    @GetMapping("/{txRef}")
    @PreAuthorize("@perm.has(authentication, 'credit:read')")
    public ResponseEntity<SuccessResponse> getPurchase(@PathVariable String txRef) {
        try {
            return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getPurchaseByTxRef(txRef)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
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
