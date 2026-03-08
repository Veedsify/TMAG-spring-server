package com.TravelMedicineAdvisory.Server.domain.blogpost;

import java.time.LocalDateTime;

public record BlogPostRequest(
    String title,
    String slug,
    String excerpt,
    String content,
    String category,
    Integer readTime,
    LocalDateTime publishedAt,
    Boolean isPublished,
    Long userId,
    Long featuredImageId
) {}
