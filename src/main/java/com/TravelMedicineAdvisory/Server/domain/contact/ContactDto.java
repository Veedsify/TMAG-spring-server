package com.TravelMedicineAdvisory.Server.domain.contact;

import java.time.LocalDateTime;

public class ContactDto {

    public record ContactRequest(
            String name,
            String email,
            String subject,
            String message,
            ContactInquiryType inquiryType
    ) {}

    public record ContactResponse(
            Long id,
            String name,
            String email,
            String subject,
            String message,
            ContactInquiryType inquiryType,
            ContactStatus status,
            LocalDateTime createdAt
    ) {}
}
