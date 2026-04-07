package com.TravelMedicineAdvisory.Server.domain.ebook;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class EbookDto {

    public record EbookVersionResponse(
            Long id,
            String label,
            String countryCode,
            String countryName,
            String region,
            BigDecimal price,
            String currency,
            String currencySymbol,
            BigDecimal fileSizeMb,
            Boolean isActive,
            Integer sortOrder
    ) {
        public static EbookVersionResponse from(EbookVersion v) {
            return new EbookVersionResponse(
                    v.getId(), v.getLabel(), v.getCountryCode(), v.getCountryName(),
                    v.getRegion(), v.getPrice(), v.getCurrency(), v.getCurrencySymbol(),
                    v.getFileSizeMb(), v.getIsActive(), v.getSortOrder()
            );
        }
    }

    public record AdminEbookVersionResponse(
            Long id,
            String label,
            String countryCode,
            String countryName,
            String region,
            BigDecimal price,
            String currency,
            String currencySymbol,
            String fileUrl,
            String fileKey,
            BigDecimal fileSizeMb,
            Boolean isActive,
            Integer sortOrder
    ) {
        public static AdminEbookVersionResponse from(EbookVersion v) {
            return new AdminEbookVersionResponse(
                    v.getId(), v.getLabel(), v.getCountryCode(), v.getCountryName(),
                    v.getRegion(), v.getPrice(), v.getCurrency(), v.getCurrencySymbol(),
                    v.getFileUrl(), v.getFileKey(), v.getFileSizeMb(), v.getIsActive(), v.getSortOrder()
            );
        }
    }

    public record AdminEbookResponse(
            Long id,
            String title,
            String slug,
            String description,
            String shortDescription,
            String author,
            String authorBio,
            String coverUrl,
            String previewUrl,
            Integer pageCount,
            Integer publishedYear,
            String isbn,
            Boolean isActive,
            Boolean isFeatured,
            List<AdminEbookVersionResponse> versions,
            LocalDateTime createdAt
    ) {
        public static AdminEbookResponse from(Ebook e, List<EbookVersion> versions) {
            return new AdminEbookResponse(
                    e.getId(), e.getTitle(), e.getSlug(), e.getDescription(),
                    e.getShortDescription(), e.getAuthor(), e.getAuthorBio(),
                    e.getCoverUrl(), e.getPreviewUrl(), e.getPageCount(),
                    e.getPublishedYear(), e.getIsbn(), e.getIsActive(), e.getIsFeatured(),
                    versions.stream().map(AdminEbookVersionResponse::from).toList(),
                    e.getCreatedAt()
            );
        }
    }

    public record EbookResponse(
            Long id,
            String title,
            String slug,
            String description,
            String shortDescription,
            String author,
            String authorBio,
            String coverUrl,
            String previewUrl,
            Integer pageCount,
            Integer publishedYear,
            String isbn,
            Boolean isActive,
            Boolean isFeatured,
            List<EbookVersionResponse> versions,
            LocalDateTime createdAt
    ) {
        public static EbookResponse from(Ebook e, List<EbookVersion> versions) {
            return new EbookResponse(
                    e.getId(), e.getTitle(), e.getSlug(), e.getDescription(),
                    e.getShortDescription(), e.getAuthor(), e.getAuthorBio(),
                    e.getCoverUrl(), e.getPreviewUrl(), e.getPageCount(),
                    e.getPublishedYear(), e.getIsbn(), e.getIsActive(), e.getIsFeatured(),
                    versions.stream().map(EbookVersionResponse::from).toList(),
                    e.getCreatedAt()
            );
        }
    }

    public record EbookOrderResponse(
            Long id,
            String txRef,
            String status,
            String buyerEmail,
            String buyerName,
            String buyerPhone,
            boolean isGuest,
            Long userId,
            Long ebookId,
            String ebookTitle,
            String ebookSlug,
            String versionLabel,
            String countryName,
            BigDecimal amount,
            BigDecimal amountPaid,
            String currency,
            String currencySymbol,
            Boolean emailSent,
            LocalDateTime paidAt,
            LocalDateTime createdAt
    ) {
        public static EbookOrderResponse from(EbookOrder o) {
            return new EbookOrderResponse(
                    o.getId(), o.getTxRef(), o.getStatus(),
                    o.getBuyerEmail(), o.getBuyerName(),
                    o.getUser() != null ? o.getUser().getPhone() : o.getGuestPhone(),
                    o.getUser() == null,
                    o.getUser() != null ? o.getUser().getId() : null,
                    o.getEbook().getId(), o.getEbook().getTitle(), o.getEbook().getSlug(),
                    o.getEbookVersion().getLabel(), o.getEbookVersion().getCountryName(),
                    o.getAmount(), o.getAmountPaid(),
                    o.getCurrency(), o.getCurrencySymbol(),
                    o.getEmailSent(), o.getPaidAt(), o.getCreatedAt()
            );
        }
    }

    public record CheckoutRequest(
            Long ebookVersionId,
            // Guest fields (optional when authenticated)
            String guestEmail,
            String guestName,
            String guestPhone
    ) {}

    public record CheckoutResponse(
            String txRef,
            String paymentLink,
            Long orderId,
            String ebookTitle,
            String versionLabel,
            BigDecimal amount,
            String currency,
            String currencySymbol
    ) {}

    public record VerifyOrderRequest(
            String txRef,
            String transactionId
    ) {}

    public record EbookStatsResponse(
            long totalOrders,
            long completedOrders,
            BigDecimal totalRevenue,
            long totalEbooks
    ) {}

    // Admin request DTOs
    public record CreateEbookRequest(
            String title,
            String slug,
            String description,
            String shortDescription,
            String author,
            String authorBio,
            String coverUrl,
            String previewUrl,
            Integer pageCount,
            Integer publishedYear,
            String isbn,
            Boolean isActive,
            Boolean isFeatured
    ) {}

    public record UpdateEbookRequest(
            String title,
            String slug,
            String description,
            String shortDescription,
            String author,
            String authorBio,
            String coverUrl,
            String previewUrl,
            Integer pageCount,
            Integer publishedYear,
            String isbn,
            Boolean isActive,
            Boolean isFeatured
    ) {}

    public record CreateVersionRequest(
            String label,
            String countryCode,
            String countryName,
            String region,
            BigDecimal price,
            String currency,
            String currencySymbol,
            String fileUrl,
            String fileKey,
            BigDecimal fileSizeMb,
            Boolean isActive,
            Integer sortOrder
    ) {}

    public record UpdateVersionRequest(
            String label,
            String countryCode,
            String countryName,
            String region,
            BigDecimal price,
            String currency,
            String currencySymbol,
            String fileUrl,
            String fileKey,
            BigDecimal fileSizeMb,
            Boolean isActive,
            Integer sortOrder
    ) {}

    // Cart DTOs
    public record CartItemResponse(
            Long id,
            Long ebookId,
            String ebookTitle,
            String ebookSlug,
            String coverUrl,
            Long ebookVersionId,
            String versionLabel,
            String countryName,
            BigDecimal price,
            String currency,
            String currencySymbol
    ) {
        public static CartItemResponse from(CartItem item) {
            return new CartItemResponse(
                    item.getId(),
                    item.getEbook().getId(),
                    item.getEbook().getTitle(),
                    item.getEbook().getSlug(),
                    item.getEbook().getCoverUrl(),
                    item.getEbookVersion().getId(),
                    item.getEbookVersion().getLabel(),
                    item.getEbookVersion().getCountryName(),
                    item.getEbookVersion().getPrice(),
                    item.getEbookVersion().getCurrency(),
                    item.getEbookVersion().getCurrencySymbol()
            );
        }
    }

    public record CartSyncItem(Long ebookVersionId) {}

    public record CartCheckoutRequest(
            List<Long> ebookVersionIds,
            String guestEmail,
            String guestName,
            String guestPhone
    ) {}

    public record CartCheckoutResponse(
            String txRef,
            String paymentLink,
            List<Long> orderIds,
            BigDecimal totalAmount,
            String currency,
            String currencySymbol,
            int itemCount
    ) {}
}
