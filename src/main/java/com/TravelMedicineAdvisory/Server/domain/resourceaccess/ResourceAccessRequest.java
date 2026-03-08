package com.TravelMedicineAdvisory.Server.domain.resourceaccess;

public record ResourceAccessRequest(
    String roleId,
    Long memberId,
    String resourceType,
    String resourceId,
    String accessType
) {}
