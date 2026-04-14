package com.TravelMedicineAdvisory.Server.domain.company;

import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanResponse;
import java.time.LocalDateTime;

public record CompanyResponse(
    Long id,
    String name,
    String industry,
    Integer totalCredits,
    Integer usedCredits,
    Integer employeeCount,
    String plan,
    String companyCode,
    Long logoId,
    BillingCurrency billingCurrency,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    CreditPlanResponse creditPlan
) {}
