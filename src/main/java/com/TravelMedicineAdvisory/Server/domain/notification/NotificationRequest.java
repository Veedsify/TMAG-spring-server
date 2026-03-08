package com.TravelMedicineAdvisory.Server.domain.notification;

public record NotificationRequest(
    String title,
    String message,
    String type,
    String link,
    Boolean isRead,
    Long userId
) {}
