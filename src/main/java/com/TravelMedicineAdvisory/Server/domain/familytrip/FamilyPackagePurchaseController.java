package com.TravelMedicineAdvisory.Server.domain.familytrip;

import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.familytrip.dto.FamilyPackageCheckoutRequest;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.TravelMedicineAdvisory.Server.security.AppUserDetails;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Family package purchases")
@RestController
@RequestMapping("/api/v1/family-package-purchases")
public class FamilyPackagePurchaseController {

    private final FamilyPackagePurchaseService service;
    private final UserRepository userRepository;
    private final com.TravelMedicineAdvisory.Server.config.CallbackRegistry callbackRegistry;

    public FamilyPackagePurchaseController(
            FamilyPackagePurchaseService service,
            UserRepository userRepository,
            com.TravelMedicineAdvisory.Server.config.CallbackRegistry callbackRegistry) {
        this.service = service;
        this.userRepository = userRepository;
        this.callbackRegistry = callbackRegistry;
    }

    @PostMapping("/checkout")
    // @PreAuthorize("@perm.has(authentication, 'family:create')")
    public ResponseEntity<SuccessResponse> initiateCheckout(
            @AuthenticationPrincipal AppUserDetails userDetails,
            @Valid @RequestBody FamilyPackageCheckoutRequest request) {
        try {
            Long userId = userDetails.getUserId();
            var result = service.initiateCheckout(userId, request);
            Map<String, Object> data = Map.of(
                    "success", true,
                    "txRef", result.txRef(),
                    "paymentLink", result.paymentLink(),
                    "packageType", result.packageType(),
                    "tripsAllowed", result.tripsAllowed(),
                    "amount", result.amountMinor(),
                    "currency", result.currency(),
                    "currencySymbol", result.currencySymbol(),
                    "purchaseId", result.purchaseId()
            );
            return ResponseEntity.ok(new SuccessResponse("Payment initiated successfully", data));
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

    @GetMapping("/callback")
    public RedirectView paymentCallback(
            @RequestParam(required = false) String tx_ref,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String transaction_id) {
        String frontendUrl = callbackRegistry.getFrontendRedirectUrl("FAMILY_PACKAGE");

        if (tx_ref == null) {
            return new RedirectView(frontendUrl + "?error=Missing%20transaction%20reference");
        }

        try {
            var result = service.verifyAndCompletePurchase(tx_ref, transaction_id);

            if ("ACTIVE".equalsIgnoreCase(result.status())) {
                String displayAmount = result.currency().equals("NGN")
                        ? "₦" + (result.amountPaidMinor() / 100)
                        : "$" + (result.amountPaidMinor() / 100);
                return new RedirectView(
                        frontendUrl
                        + "?success=true"
                        + "&tx_ref=" + tx_ref
                        + "&packageType=" + result.packageType()
                        + "&tripsAllowed=" + result.tripsAllowed()
                        + "&amount=" + displayAmount
                        + "&status=" + result.status()
                );
            } else {
                String errorMsg = result.status() != null ? "Payment%20" + result.status() : "Payment%20not%20completed";
                return new RedirectView(frontendUrl + "?success=false&error=" + errorMsg + "&tx_ref=" + tx_ref);
            }
        } catch (NoSuchElementException e) {
            return new RedirectView(frontendUrl + "?error=Transaction%20not%20found&tx_ref=" + tx_ref);
        } catch (Exception e) {
            return new RedirectView(frontendUrl + "?error=Payment%20verification%20failed&tx_ref=" + tx_ref);
        }
    }

    @GetMapping("/active")
    @PreAuthorize("@perm.has(authentication, 'family:read')")
    public ResponseEntity<SuccessResponse> getActivePurchase(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return service.getActivePurchase(userId)
                .map(p -> ResponseEntity.ok(new SuccessResponse("Active purchase found", p)))
                .orElse(ResponseEntity.ok(new SuccessResponse("No active purchase", Map.of("activePurchase", null))));
    }

    @GetMapping("/history")
    @PreAuthorize("@perm.has(authentication, 'family:read')")
    public ResponseEntity<SuccessResponse> getPurchaseHistory(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getPurchaseHistory(userId)));
    }

    private Long getUserId(UserDetails userDetails) {
        if (userDetails instanceof AppUserDetails appUserDetails && appUserDetails.getUserId() != null) {
            return appUserDetails.getUserId();
        }
        String email = userDetails.getUsername();
        return userRepository.findByEmail(email).map(u -> u.getId())
                .orElseThrow(() -> new RuntimeException("Unable to extract user ID"));
    }
}
