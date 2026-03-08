package com.TravelMedicineAdvisory.Server.core.types;

public record PaginatedResponse<T>(T data, Pagination pagination) {}
