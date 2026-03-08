package com.TravelMedicineAdvisory.Server.core.types;

public record SuccessResponse(String message, boolean success, Object data) {
    public SuccessResponse(String message, Object data) {
        this(message, true, data);
    }

    public SuccessResponse(Object data) {
        this(null, true, data);
    }
}
