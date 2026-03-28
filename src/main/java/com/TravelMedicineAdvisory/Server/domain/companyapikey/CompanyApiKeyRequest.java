package com.TravelMedicineAdvisory.Server.domain.companyapikey;

import java.time.LocalDateTime;

public record CompanyApiKeyRequest(
    String name,
    Long companyId,
    LocalDateTime expiresAt
) {}
