package com.TravelMedicineAdvisory.Server.domain.companyapikey;

public record CompanyApiKeyResponse(
    Long id,
    String name,
    String keyPrefix,
    String status,
    String lastUsedAt,
    String expiresAt,
    String createdAt
) {}
