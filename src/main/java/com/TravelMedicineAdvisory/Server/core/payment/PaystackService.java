package com.TravelMedicineAdvisory.Server.core.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.*;

@Service
public class PaystackService {

    private static final Logger logger = LoggerFactory.getLogger(PaystackService.class);
    private static final String PAYSTACK_BASE_URL = "https://api.paystack.co";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final WebClient webClient;

    @Value("${app.payment.paystack.secret-key:}")
    private String secretKey;

    public PaystackService() {
        this.webClient = WebClient.builder()
                .baseUrl(PAYSTACK_BASE_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public boolean isConfigured() {
        return secretKey != null && !secretKey.isBlank();
    }

    /**
     * Fetches the list of Nigerian banks from Paystack.
     * Paystack uses standard CBN NUBAN bank codes (e.g. "044" for Access Bank)
     * that are consistent with their account resolve endpoint.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getBanks() {
        try {
            Map<String, Object> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/bank")
                            .queryParam("country", "nigeria")
                            .queryParam("perPage", "200")
                            .queryParam("use_cursor", "false")
                            .build())
                    .header("Authorization", "Bearer " + secretKey)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(TIMEOUT)
                    .block();

            if (response != null && Boolean.TRUE.equals(response.get("status"))) {
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
            logger.warn("Unexpected Paystack bank list response: {}", response);
        } catch (Exception e) {
            logger.error("Failed to fetch banks from Paystack: {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    /**
     * Resolves a Nigerian bank account number using Paystack's account verification.
     * Uses GET /bank/resolve?account_number=...&bank_code=...
     *
     * @throws IllegalArgumentException if the account cannot be resolved
     * @throws IllegalStateException    if Paystack is not configured
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> resolveAccount(String accountNumber, String bankCode) {
        if (!isConfigured()) {
            throw new IllegalStateException("Paystack is not configured. Add PAYSTACK_SECRET_KEY to resolve NGN bank accounts.");
        }
        if (accountNumber == null || accountNumber.isBlank() || bankCode == null || bankCode.isBlank()) {
            throw new IllegalArgumentException("Account number and bank code are required.");
        }

        try {
            logger.info("Resolving account via Paystack: bank={}, account={}", bankCode, accountNumber);

            Map<String, Object> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/bank/resolve")
                            .queryParam("account_number", accountNumber)
                            .queryParam("bank_code", bankCode)
                            .build())
                    .header("Authorization", "Bearer " + secretKey)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(Map.class)
                                    .defaultIfEmpty(Collections.emptyMap())
                                    .map(errorBody -> {
                                        Object msg = errorBody.get("message");
                                        String errorMessage = msg != null
                                                ? String.valueOf(msg)
                                                : "Unable to validate account.";
                                        logger.warn("Paystack resolve error ({}): {}", clientResponse.statusCode(), errorMessage);
                                        return new IllegalArgumentException(errorMessage);
                                    })
                    )
                    .bodyToMono(Map.class)
                    .timeout(TIMEOUT)
                    .block();

            logger.info("Paystack resolve response: {}", response);

            if (response != null && Boolean.TRUE.equals(response.get("status"))) {
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
            logger.error("Failed to resolve account via Paystack: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Account validation failed: " + e.getMessage());
        }
    }
}
