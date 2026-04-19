package com.TravelMedicineAdvisory.Server.core.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;

public record FlutterwaveApiResponse(
        @JsonProperty("status") String status,
        @JsonProperty("message") String message,
        @JsonProperty("data") JsonNode data
) {
    /**
     * Returns the primary data object. Handles both single-object responses and
     * array responses (e.g., from /transactions?tx_ref=...) by using the first element.
     */
    private JsonNode dataObjectOrFirstElement() {
        if (data == null || data.isNull() || data.isMissingNode()) {
            return null;
        }
        if (data.isObject()) {
            return data;
        }
        if (data.isArray() && !data.isEmpty()) {
            JsonNode first = data.get(0);
            return first != null && first.isObject() ? first : null;
        }
        return null;
    }

    public String getTxRef() {
        return textAt(dataObjectOrFirstElement(), "tx_ref");
    }

    public String getFlwRef() {
        return textAt(dataObjectOrFirstElement(), "flw_ref");
    }

    public String getPaymentLink() {
        return textAt(dataObjectOrFirstElement(), "link");
    }

    public BigDecimal getAmount() {
        JsonNode n = dataObjectOrFirstElement();
        if (n == null || !n.has("amount")) {
            return null;
        }
        JsonNode amount = n.get("amount");
        if (amount == null || amount.isNull()) {
            return null;
        }
        if (amount.isNumber()) {
            return BigDecimal.valueOf(amount.asDouble());
        }
        if (amount.isTextual()) {
            return new BigDecimal(amount.asText());
        }
        return null;
    }

    public String getCurrency() {
        return textAt(dataObjectOrFirstElement(), "currency");
    }

    /** Charge/transaction status from nested {@code data}, not the top-level API {@code status}. */
    public String getStatus() {
        return textAt(dataObjectOrFirstElement(), "status");
    }

    public boolean isSuccessful() {
        return "success".equalsIgnoreCase(status);
    }

    private static String textAt(JsonNode node, String field) {
        if (node == null || !node.has(field)) {
            return null;
        }
        JsonNode v = node.get(field);
        if (v == null || v.isNull() || v.isMissingNode()) {
            return null;
        }
        return v.asText();
    }
}
