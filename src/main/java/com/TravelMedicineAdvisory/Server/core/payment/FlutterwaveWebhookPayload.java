package com.TravelMedicineAdvisory.Server.core.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Map;

public record FlutterwaveWebhookPayload(
    @JsonProperty("id") int id,
    @JsonProperty("txRef") String txRef,
    @JsonProperty("flwRef") String flwRef,
    @JsonProperty("orderRef") String orderRef,
    @JsonProperty("flwWebhookSignature") String flwWebhookSignature,
    @JsonProperty("deviceFingerprint") String deviceFingerprint,
    @JsonProperty("amount") BigDecimal amount,
    @JsonProperty("currency") String currency,
    @JsonProperty("chargedAmount") BigDecimal chargedAmount,
    @JsonProperty("appFee") BigDecimal appFee,
    @JsonProperty("merchantFee") BigDecimal merchantFee,
    @JsonProperty("merchantBearsFee") boolean merchantBearsFee,
    @JsonProperty("processorResponse") String processorResponse,
    @JsonProperty("authModel") String authModel,
    @JsonProperty("ip") String ip,
    @JsonProperty("narration") String narration,
    @JsonProperty("status") String status,
    @JsonProperty("authURL") String authURL,
    @JsonProperty("completeUrl") String completeUrl,
    @JsonProperty("errorCode") String errorCode,
    @JsonProperty("error") String error,
    @JsonProperty("customer") Map<String, Object> customer,
    @JsonProperty("entity") Map<String, Object> entity
) {}
