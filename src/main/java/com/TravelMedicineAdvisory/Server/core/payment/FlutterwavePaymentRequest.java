package com.TravelMedicineAdvisory.Server.core.payment;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record FlutterwavePaymentRequest(
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "100", message = "Minimum amount is 100")
    BigDecimal amount,
    
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    String currency,
    
    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    String customerEmail,
    
    @NotBlank(message = "Customer name is required")
    String customerName,
    
    @NotBlank(message = "Payment description is required")
    String description,
    
    @NotBlank(message = "Transaction reference is required")
    String txRef,
    
    String customerPhone,
    
    String redirectUrl,
    
    @DecimalMin(value = "1", message = "Minimum quantity is 1")
    Integer quantity,
    
    String packageId,
    
    String userId,
    
    String companyId
) {}
