package com.TravelMedicineAdvisory.Server.core.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record FlutterwaveApiResponse(
    @JsonProperty("status") String status,
    @JsonProperty("message") String message,
    @JsonProperty("data") Object data
) {
    /**
     * Returns the data as a Map. Handles both single-object responses and
     * array responses (e.g., from /transactions?tx_ref=...) by extracting
     * the first element from the array.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getDataMap() {
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        }
        if (data instanceof List<?> list && !list.isEmpty()) {
            Object first = list.getFirst();
            if (first instanceof Map) {
                return (Map<String, Object>) first;
            }
        }
        return null;
    }

    public String getTxRef() {
        var map = getDataMap();
        return map != null ? (String) map.get("tx_ref") : null;
    }

    public String getFlwRef() {
        var map = getDataMap();
        return map != null ? (String) map.get("flw_ref") : null;
    }

    public String getPaymentLink() {
        var map = getDataMap();
        return map != null ? (String) map.get("link") : null;
    }

    public BigDecimal getAmount() {
        var map = getDataMap();
        if (map == null) return null;
        Object amount = map.get("amount");
        if (amount instanceof BigDecimal) return (BigDecimal) amount;
        if (amount instanceof Number) return BigDecimal.valueOf(((Number) amount).doubleValue());
        if (amount instanceof String) return new BigDecimal((String) amount);
        return null;
    }

    public String getCurrency() {
        var map = getDataMap();
        return map != null ? (String) map.get("currency") : null;
    }

    public String getStatus() {
        var map = getDataMap();
        return map != null ? (String) map.get("status") : null;
    }

    public boolean isSuccessful() {
        return "success".equalsIgnoreCase(status);
    }
}
