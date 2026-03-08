package com.TravelMedicineAdvisory.Server.domain.invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvoiceRequest(
    BigDecimal amount,
    String currency,
    String status,
    String description,
    LocalDateTime issuedAt,
    LocalDateTime dueDate,
    LocalDateTime paidAt,
    String paymentMethod,
    Long companyId,
    Long userId
) {}
