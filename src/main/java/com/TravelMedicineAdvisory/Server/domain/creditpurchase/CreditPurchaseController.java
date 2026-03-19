package com.TravelMedicineAdvisory.Server.domain.creditpurchase;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.TravelMedicineAdvisory.Server.security.AppUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/credit-purchases")
public class CreditPurchaseController {

    private final CreditPurchaseService service;
    private final UserRepository userRepository;

    public CreditPurchaseController(CreditPurchaseService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    @PostMapping("/initiate")
    public ResponseEntity<SuccessResponse> initiatePurchase(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreditPurchaseRequest request) {
        try {
            Long userId = getUserIdFromUserDetails(userDetails);
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
    public ResponseEntity<SuccessResponse> paymentCallback(
            @RequestParam(required = false) String tx_ref,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String transaction_id) {
        if (tx_ref == null) {
            return ResponseEntity.badRequest().body(new SuccessResponse("Missing transaction reference", null));
        }

        try {
            var result = service.verifyAndCompletePurchase(tx_ref, transaction_id);
            return ResponseEntity.ok(new SuccessResponse("Callback processed", Map.of("success", "completed".equals(result.status()), "purchase", result)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/history")
    public ResponseEntity<SuccessResponse> getPurchaseHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getPurchaseHistory(userId)));
    }

    @GetMapping("/{txRef}")
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
