package com.TravelMedicineAdvisory.Server.core.payment;

import java.math.BigDecimal;

public record FlutterwavePaymentResponse(
    boolean success,
    String message,
    String txRef,
    String flwRef,
    String status,
    String raveReference,
    BigDecimal amount,
    String currency,
    String paymentLink,
    String customerEmail,
    Long invoiceId
) {
    public static FlutterwavePaymentResponse success(
            String txRef,
            String flwRef,
            String status,
            BigDecimal amount,
            String currency,
            String paymentLink,
            String customerEmail) {
        return new FlutterwavePaymentResponse(
            true,
            "Payment initiated successfully",
            txRef,
            flwRef,
            status,
            null,
            amount,
            currency,
            paymentLink,
            customerEmail,
            null
        );
    }
    
    public static FlutterwavePaymentResponse error(String message, String txRef) {
        return new FlutterwavePaymentResponse(
            false,
            message,
            txRef,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }
}
