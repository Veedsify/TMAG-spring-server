package com.TravelMedicineAdvisory.Server.core.payment;

import com.TravelMedicineAdvisory.Server.domain.creditpurchase.CreditPurchaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private final FlutterwaveService flutterwaveService;
    private final CreditPurchaseService creditPurchaseService;
    private final ObjectMapper objectMapper;

    public PaymentController(
            FlutterwaveService flutterwaveService,
            CreditPurchaseService creditPurchaseService,
            ObjectMapper objectMapper) {
        this.flutterwaveService = flutterwaveService;
        this.creditPurchaseService = creditPurchaseService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/initiate")
    public ResponseEntity<?> initiatePayment(@RequestBody FlutterwavePaymentRequest request) {
        if (!flutterwaveService.isConfigured()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Payment provider not configured"));
        }
        
        FlutterwavePaymentResponse response = flutterwaveService.initiatePayment(request);
        
        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/verify/{transactionId}")
    public ResponseEntity<?> verifyTransaction(@PathVariable String transactionId) {
        FlutterwavePaymentResponse response = flutterwaveService.verifyTransaction(transactionId);
        
        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/verify-by-reference/{txRef}")
    public ResponseEntity<?> verifyTransactionByReference(@PathVariable String txRef) {
        FlutterwavePaymentResponse response = flutterwaveService.verifyTransactionByReference(txRef);
        
        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/refund/{transactionId}")
    public ResponseEntity<?> refundTransaction(
            @PathVariable String transactionId,
            @RequestParam(required = false) BigDecimal amount) {
        FlutterwavePaymentResponse response = flutterwaveService.refundTransaction(transactionId, amount);

        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/webhook/flutterwave")
    public ResponseEntity<?> handleFlutterwaveWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "verif-hash", required = false) String verifHash) {

        logger.info("Received Flutterwave webhook");

        if (!flutterwaveService.validateWebhookSignature(verifHash)) {
            logger.warn("Invalid Flutterwave webhook verif-hash");
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "Invalid signature"));
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payloadMap = objectMapper.readValue(payload, Map.class);

            String event = (String) payloadMap.get("event");
            if (!"charge.completed".equals(event)) {
                logger.info("Ignoring webhook event: {}", event);
                return ResponseEntity.ok(Map.of("success", true, "message", "Event ignored"));
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) payloadMap.get("data");
            if (data == null) {
                logger.warn("Webhook payload missing 'data' field");
                return ResponseEntity.ok(Map.of("success", true, "message", "No data"));
            }

            String txRef = (String) data.get("tx_ref");
            String flwRef = (String) data.get("flw_ref");
            String status = (String) data.get("status");

            Object amountObj = data.get("amount");
            BigDecimal amount = BigDecimal.ZERO;
            if (amountObj instanceof BigDecimal bd) {
                amount = bd;
            } else if (amountObj instanceof Number n) {
                amount = BigDecimal.valueOf(n.doubleValue());
            } else if (amountObj instanceof String s) {
                amount = new BigDecimal(s);
            }

            logger.info("Processing webhook: txRef={}, flwRef={}, status={}, amount={}", txRef, flwRef, status, amount);

            var result = creditPurchaseService.completePurchaseFromWebhook(txRef, flwRef, status, amount);

            if (result != null) {
                logger.info("Webhook processed successfully for txRef: {}", txRef);
            } else {
                logger.warn("Purchase not found for txRef: {}", txRef);
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Webhook processed"));
        } catch (Exception e) {
            logger.error("Error processing Flutterwave webhook: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "Error processing webhook"));
        }
    }

    @GetMapping("/config")
    public ResponseEntity<?> getPaymentConfig() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", Map.of(
                "provider", "flutterwave",
                "publicKey", flutterwaveService.getPublicKey() != null ? "set" : "not_set",
                "configured", flutterwaveService.isConfigured()
            )
        ));
    }

    @GetMapping("/generate-reference")
    public ResponseEntity<?> generateReference() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", Map.of(
                "txRef", flutterwaveService.generateTransactionReference()
            )
        ));
    }
}
