package com.TravelMedicineAdvisory.Server.core.payment;

import java.math.BigDecimal;
import java.util.Map;

public record FlutterwaveInitiatePaymentRequest(
    BigDecimal amount,
    String currency,
    String txRef,
    String orderRef,
    String paymentOptions,
    String redirectUrl,
    String customerName,
    String customerEmail,
    String customerPhone,
    String title,
    String description,
    String logo,
    Map<String, String> meta
) {}
