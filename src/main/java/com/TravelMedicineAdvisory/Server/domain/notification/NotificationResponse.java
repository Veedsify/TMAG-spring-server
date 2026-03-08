package com.TravelMedicineAdvisory.Server.domain.notification;

import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    String title,
    String message,
    String type,
    String link,
    Boolean isRead,
    Long userId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
