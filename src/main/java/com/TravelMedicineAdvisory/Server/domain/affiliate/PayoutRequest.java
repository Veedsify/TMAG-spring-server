package com.TravelMedicineAdvisory.Server.domain.affiliate;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PayoutRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1.00", message = "Minimum payout amount is 1.00")
        BigDecimal amount,
        String currency,
        @NotBlank(message = "Payment method is required")
        @JsonProperty("payment_method") String paymentMethod,
        @NotBlank(message = "Payment details are required")
        @JsonProperty("payment_details") String paymentDetails
) {}
