package com.TravelMedicineAdvisory.Server.domain.invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvoiceRevenueProjection(
        BigDecimal amount,
        String currency,
        LocalDateTime paidAt) {
}
