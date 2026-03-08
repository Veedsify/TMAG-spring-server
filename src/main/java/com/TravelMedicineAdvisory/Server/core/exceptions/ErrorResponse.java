package com.TravelMedicineAdvisory.Server.core.exceptions;

public record ErrorResponse(String error, boolean success, Object details) {
    public ErrorResponse(String error) {
        this(error, false, null);
    }
    
    public ErrorResponse(String error, Object details) {
        this(error, false, details);
    }
}
