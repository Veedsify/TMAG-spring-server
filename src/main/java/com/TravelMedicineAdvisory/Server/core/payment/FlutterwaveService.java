package com.TravelMedicineAdvisory.Server.core.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;

@Service
public class FlutterwaveService {

    private static final Logger logger = LoggerFactory.getLogger(FlutterwaveService.class);
    private static final String FLUTTERWAVE_BASE_URL = "https://api.flutterwave.com/v3";

    private final WebClient webClient;

    @Value("${app.payment.flutterwave.public-key}")
    private String publicKey;

    @Value("${app.payment.flutterwave.secret-key}")
    private String secretKey;

    @Value("${app.payment.flutterwave.encryption-key}")
    private String encryptionKey;

    @Value("${app.payment.flutterwave.webhook-secret-hash:}")
    private String webhookSecretHash;

    @Value("${app.payment.flutterwave.timeout:30s}")
    private Duration timeout;

    public FlutterwaveService() {
        this.webClient = WebClient.builder()
                .baseUrl(FLUTTERWAVE_BASE_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Fetches the list of banks for a given country from Flutterwave.
     * Used for affiliate payout bank selection.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getBanks(String country) {
        try {
            String countryParam = country != null && !country.isBlank() ? country.toUpperCase() : "NG";
            Map<String, Object> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/banks/{country}")
                            .build(countryParam))
                    .header("Authorization", "Bearer " + secretKey)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(timeout)
                    .block();

            if (response != null && "success".equals(response.get("status"))) {
                Object data = response.get("data");
                if (data instanceof List<?> list) {
                    List<Map<String, Object>> banks = new ArrayList<>();
                    for (Object item : list) {
                        if (item instanceof Map<?, ?> bank) {
                            Map<String, Object> bankInfo = new LinkedHashMap<>();
                            bankInfo.put("id", bank.get("id"));
                            bankInfo.put("code", bank.get("code"));
                            bankInfo.put("name", bank.get("name"));
                            banks.add(bankInfo);
                        }
                    }
                    return banks;
                }
            }
        } catch (Exception e) {
            logger.error("Failed to fetch banks from Flutterwave: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Validates a bank account number using Flutterwave's account verification.
     *
     * @throws IllegalArgumentException if the account cannot be resolved
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> resolveAccount(String accountNumber, String bankCode) {
        if (!isConfigured()) {
            throw new IllegalStateException("Flutterwave is not configured. Account validation is unavailable.");
        }
        if (accountNumber == null || accountNumber.isBlank() || bankCode == null || bankCode.isBlank()) {
            throw new IllegalArgumentException("Account number and bank code are required.");
        }

        try {
            Map<String, Object> body = Map.of(
                    "account_number", accountNumber,
                    "account_bank", bankCode
            );
            logger.info("Resolving account via Flutterwave: bank={}, account={}", bankCode, accountNumber);

            Map<String, Object> response = webClient.post()
                    .uri("/accounts/resolve")
                    .header("Authorization", "Bearer " + secretKey)
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(Map.class)
                                    .defaultIfEmpty(Collections.emptyMap())
                                    .map(errorBody -> {
                                        Object msg = errorBody.get("message");
                                        String errorMessage = msg != null ? String.valueOf(msg) : "Unable to validate account.";
                                        logger.warn("Flutterwave resolve error ({}): {}", clientResponse.statusCode(), errorMessage);
                                        return new IllegalArgumentException(errorMessage);
                                    })
                    )
                    .bodyToMono(Map.class)
                    .timeout(timeout)
                    .block();

            logger.info("Flutterwave resolve response: {}", response);

            if (response != null && "success".equals(response.get("status"))) {
                Object data = response.get("data");
                if (data instanceof Map<?, ?> accountData) {
                    Map<String, String> result = new LinkedHashMap<>();
                    result.put("accountName", String.valueOf(accountData.get("account_name")));
                    result.put("accountNumber", String.valueOf(accountData.get("account_number")));
                    return result;
                }
            }

            String message = response != null && response.get("message") != null
                    ? String.valueOf(response.get("message"))
                    : "Unable to validate account. Please check your details and try again.";
            throw new IllegalArgumentException(message);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to resolve account: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Account validation failed: " + e.getMessage());
        }
    }

    public FlutterwavePaymentResponse initiatePayment(FlutterwavePaymentRequest request) {
        try {
            Map<String, Object> paymentData = buildPaymentPayload(request);

            FlutterwaveApiResponse response = webClient.post()
                    .uri("/payments")
                    .header("Authorization", "Bearer " + secretKey)
                    .bodyValue(paymentData)
                    .retrieve()
                    .bodyToMono(FlutterwaveApiResponse.class)
                    .block(timeout);

            if (response != null && response.isSuccessful()) {
                logger.info("Flutterwave payment initiated successfully. txRef: {}", request.txRef());
                return FlutterwavePaymentResponse.success(
                        request.txRef(),
                        response.getFlwRef(),
                        response.getStatus(),
                        response.getAmount(),
                        response.getCurrency(),
                        response.getPaymentLink(),
                        request.customerEmail());
            } else {
                logger.error("Failed to initiate Flutterwave payment: {}",
                        response != null ? response.message() : "Unknown error");
                return FlutterwavePaymentResponse.error(
                        response != null ? response.message() : "Failed to initiate payment",
                        request.txRef());
            }
        } catch (Exception e) {
            logger.error("Error initiating Flutterwave payment: {}", e.getMessage(), e);
            return FlutterwavePaymentResponse.error(e.getMessage(), request.txRef());
        }
    }

    public FlutterwavePaymentResponse verifyTransaction(String transactionId) {
        try {
            FlutterwaveApiResponse response = webClient.get()
                    .uri("/transactions/{id}/verify", transactionId)
                    .header("Authorization", "Bearer " + secretKey)
                    .retrieve()
                    .bodyToMono(FlutterwaveApiResponse.class)
                    .block(timeout);

            if (response != null && response.isSuccessful()) {
                return FlutterwavePaymentResponse.success(
                        response.getTxRef(),
                        response.getFlwRef(),
                        response.getStatus(),
                        response.getAmount(),
                        response.getCurrency(),
                        null,
                        null);
            } else {
                return FlutterwavePaymentResponse.error(
                        response != null ? response.message() : "Verification failed",
                        null);
            }
        } catch (Exception e) {
            logger.error("Error verifying Flutterwave transaction: {}", e.getMessage(), e);
            return FlutterwavePaymentResponse.error(e.getMessage(), null);
        }
    }

    public FlutterwavePaymentResponse verifyTransactionByReference(String txRef) {
        try {
            FlutterwaveApiResponse response = webClient.get()
                    .uri("/transactions?tx_ref={txRef}", txRef)
                    .header("Authorization", "Bearer " + secretKey)
                    .retrieve()
                    .bodyToMono(FlutterwaveApiResponse.class)
                    .block(timeout);

            if (response != null && response.isSuccessful()) {
                return FlutterwavePaymentResponse.success(
                        txRef,
                        response.getFlwRef(),
                        response.getStatus(),
                        response.getAmount(),
                        response.getCurrency(),
                        null,
                        null);
            } else {
                return FlutterwavePaymentResponse.error(
                        response != null ? response.message() : "Verification failed",
                        txRef);
            }
        } catch (Exception e) {
            logger.error("Error verifying Flutterwave transaction by reference: {}", e.getMessage(), e);
            return FlutterwavePaymentResponse.error(e.getMessage(), txRef);
        }
    }

    /**
     * Validates a Flutterwave webhook by comparing the verif-hash header
     * against the secret hash configured in the Flutterwave dashboard.
     *
     * @param verifHash the value of the "verif-hash" header from the webhook request
     * @return true if the hash matches the configured webhook secret hash
     */
    public boolean validateWebhookSignature(String verifHash) {
        if (webhookSecretHash == null || webhookSecretHash.isBlank()) {
            logger.warn("Webhook secret hash is not configured — rejecting webhook");
            return false;
        }
        if (verifHash == null || verifHash.isBlank()) {
            logger.warn("Missing verif-hash header in webhook request");
            return false;
        }
        return MessageDigest.isEqual(
                webhookSecretHash.getBytes(),
                verifHash.getBytes()
        );
    }

    public String generateTransactionReference() {
        return "TMAG_" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    public Map<String, Object> buildPaymentPayload(FlutterwavePaymentRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("amount", request.amount());
        payload.put("currency", request.currency());
        payload.put("tx_ref", request.txRef());
        payload.put("payment_options", "card,ussd,account,transfer,mobilemoney");
        if (request.redirectUrl() == null || request.redirectUrl().isBlank()) {
            throw new IllegalArgumentException("redirectUrl is required for Flutterwave payment initiation");
        }
        payload.put("redirect_url", request.redirectUrl());

        Map<String, Object> customer = new HashMap<>();
        customer.put("email", request.customerEmail());
        customer.put("name", request.customerName());
        customer.put("phonenumber", request.customerPhone() != null ? request.customerPhone() : "");
        payload.put("customer", customer);

        Map<String, Object> meta = new HashMap<>();
        meta.put("consumer_id", request.userId() != null ? request.userId() : "");
        meta.put("consumer_mac", request.companyId() != null ? request.companyId() : "");
        if (request.packageId() != null) {
            meta.put("package_id", request.packageId());
        }
        payload.put("meta", meta);

        payload.put("description", request.description());

        return payload;
    }

    /**
     * Refunds a transaction. Uses POST /v3/transactions/{id}/refund.
     *
     * @param transactionId the Flutterwave transaction ID (numeric)
     * @param amount optional partial refund amount; null for full refund
     */
    public FlutterwavePaymentResponse refundTransaction(String transactionId, BigDecimal amount) {
        try {
            Map<String, Object> refundData = new HashMap<>();
            if (amount != null) {
                refundData.put("amount", amount);
            }

            FlutterwaveApiResponse response = webClient.post()
                    .uri("/transactions/{id}/refund", transactionId)
                    .header("Authorization", "Bearer " + secretKey)
                    .bodyValue(refundData)
                    .retrieve()
                    .bodyToMono(FlutterwaveApiResponse.class)
                    .block(timeout);

            if (response != null && response.isSuccessful()) {
                return FlutterwavePaymentResponse.success(
                        null,
                        null,
                        "refunded",
                        response.getAmount(),
                        response.getCurrency(),
                        null,
                        null);
            } else {
                return FlutterwavePaymentResponse.error(
                        response != null ? response.message() : "Refund failed",
                        null);
            }
        } catch (Exception e) {
            logger.error("Error refunding Flutterwave transaction: {}", e.getMessage(), e);
            return FlutterwavePaymentResponse.error(e.getMessage(), null);
        }
    }

    public Mono<FlutterwaveApiResponse> initiatePaymentAsync(FlutterwavePaymentRequest request) {
        Map<String, Object> paymentData = buildPaymentPayload(request);

        return webClient.post()
                .uri("/payments")
                .header("Authorization", "Bearer " + secretKey)
                .bodyValue(paymentData)
                .retrieve()
                .bodyToMono(FlutterwaveApiResponse.class)
                .timeout(timeout)
                .onErrorResume(e -> {
                    logger.error("Async payment initiation error: {}", e.getMessage());
                    return Mono.just(new FlutterwaveApiResponse("error", e.getMessage(), null));
                });
    }

    public boolean isConfigured() {
        return secretKey != null && !secretKey.isEmpty();
    }

    public String getPublicKey() {
        return publicKey;
    }
}
