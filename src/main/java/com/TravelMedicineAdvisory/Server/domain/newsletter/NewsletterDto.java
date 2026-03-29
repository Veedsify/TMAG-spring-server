package com.TravelMedicineAdvisory.Server.domain.newsletter;

import java.time.LocalDateTime;

public class NewsletterDto {

    public record NewsletterRequest(String email) {}

    public record NewsletterResponse(
            Long id,
            String email,
            Boolean isActive,
            LocalDateTime subscribedAt
    ) {}
}
