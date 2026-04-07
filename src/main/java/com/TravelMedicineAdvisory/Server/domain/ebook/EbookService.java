package com.TravelMedicineAdvisory.Server.domain.ebook;

import com.TravelMedicineAdvisory.Server.core.payment.FlutterwavePaymentRequest;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwavePaymentResponse;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwaveService;
import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@Transactional
public class EbookService {

    private static final Logger logger = LoggerFactory.getLogger(EbookService.class);

    private final EbookRepository ebookRepository;
    private final EbookVersionRepository versionRepository;
    private final EbookOrderRepository orderRepository;
    private final UserRepository userRepository;
    private final FlutterwaveService flutterwaveService;
    private final QueueService queueService;

    @Value("${app.payment.flutterwave.ebook-callback-url:http://localhost:3000/shop/order-confirmation}")
    private String ebookCallbackUrl;

    public EbookService(EbookRepository ebookRepository,
                        EbookVersionRepository versionRepository,
                        EbookOrderRepository orderRepository,
                        UserRepository userRepository,
                        FlutterwaveService flutterwaveService,
                        QueueService queueService) {
        this.ebookRepository = ebookRepository;
        this.versionRepository = versionRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.flutterwaveService = flutterwaveService;
        this.queueService = queueService;
    }

    // ─── Public: Ebook listing ────────────────────────────────────────────────

    public List<EbookDto.EbookResponse> listActiveEbooks() {
        return ebookRepository.findAllByIsActiveTrueOrderBySortOrderAsc().stream()
                .map(e -> EbookDto.EbookResponse.from(e,
                        versionRepository.findByEbookIdAndIsActiveTrueOrderBySortOrderAsc(e.getId())))
                .toList();
    }

    public EbookDto.EbookResponse getEbookBySlug(String slug) {
        Ebook ebook = ebookRepository.findBySlug(slug)
                .orElseThrow(() -> new NoSuchElementException("Ebook not found"));
        List<EbookVersion> versions = versionRepository.findByEbookIdAndIsActiveTrueOrderBySortOrderAsc(ebook.getId());
        return EbookDto.EbookResponse.from(ebook, versions);
    }

    // ─── Checkout ────────────────────────────────────────────────────────────

    public EbookDto.CheckoutResponse initiateCheckout(Long userId, EbookDto.CheckoutRequest request) {
        EbookVersion version = versionRepository.findById(request.ebookVersionId())
                .orElseThrow(() -> new NoSuchElementException("Ebook version not found"));

        if (!Boolean.TRUE.equals(version.getIsActive())) {
            throw new IllegalStateException("This ebook version is not available for purchase");
        }

        Ebook ebook = version.getEbook();
        if (!Boolean.TRUE.equals(ebook.getIsActive())) {
            throw new IllegalStateException("This ebook is not available");
        }

        String buyerEmail;
        String buyerName;
        String buyerPhone;
        User user = null;

        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
            buyerEmail = user.getEmail();
            String first = user.getFirstName() != null ? user.getFirstName() : "";
            String last = user.getLastName() != null ? user.getLastName() : "";
            buyerName = (first + " " + last).trim();
            buyerPhone = user.getPhone();
        } else {
            if (request.guestEmail() == null || request.guestEmail().isBlank()) {
                throw new IllegalArgumentException("Email is required for guest checkout");
            }
            if (request.guestName() == null || request.guestName().isBlank()) {
                throw new IllegalArgumentException("Name is required for guest checkout");
            }
            buyerEmail = request.guestEmail().trim().toLowerCase();
            buyerName = request.guestName().trim();
            buyerPhone = request.guestPhone();
        }

        String txRef = flutterwaveService.generateTransactionReference();

        EbookOrder order = new EbookOrder();
        order.setTxRef(txRef);
        order.setEbook(ebook);
        order.setEbookVersion(version);
        order.setAmount(version.getPrice());
        order.setCurrency(version.getCurrency());
        order.setCurrencySymbol(version.getCurrencySymbol());
        order.setStatus("pending");

        if (user != null) {
            order.setUser(user);
        } else {
            order.setGuestEmail(buyerEmail);
            order.setGuestName(buyerName);
            order.setGuestPhone(buyerPhone);
        }

        orderRepository.save(order);

        String callbackWithRef = ebookCallbackUrl + "?txRef=" + txRef;

        FlutterwavePaymentRequest paymentRequest = new FlutterwavePaymentRequest(
                version.getPrice(),
                version.getCurrency(),
                buyerEmail,
                buyerName,
                "TMAG Ebook: " + ebook.getTitle() + " — " + version.getLabel(),
                txRef,
                buyerPhone,
                callbackWithRef,
                null,
                null,
                userId != null ? userId.toString() : "guest",
                null
        );

        FlutterwavePaymentResponse paymentResponse = flutterwaveService.initiatePayment(paymentRequest);

        if (!paymentResponse.success() || paymentResponse.paymentLink() == null) {
            order.setStatus("failed");
            order.setFailedReason("Payment initiation failed: " + paymentResponse.message());
            order.setFailedAt(LocalDateTime.now());
            orderRepository.save(order);
            throw new RuntimeException("Failed to initiate payment: " + paymentResponse.message());
        }

        return new EbookDto.CheckoutResponse(
                txRef, paymentResponse.paymentLink(), order.getId(),
                ebook.getTitle(), version.getLabel(),
                version.getPrice(), version.getCurrency(), version.getCurrencySymbol()
        );
    }

    // ─── Order verification & completion ─────────────────────────────────────

    public EbookDto.EbookOrderResponse verifyOrder(String txRef, String transactionId) {
        List<EbookOrder> orders = orderRepository.findAllByTxRef(txRef);
        if (orders.isEmpty()) {
            throw new NoSuchElementException("Order not found");
        }

        EbookOrder firstOrder = orders.get(0);

        if ("completed".equalsIgnoreCase(firstOrder.getStatus())) {
            return EbookDto.EbookOrderResponse.from(firstOrder);
        }

        FlutterwavePaymentResponse verification;
        if (transactionId != null && !transactionId.isBlank()) {
            verification = flutterwaveService.verifyTransaction(transactionId);
        } else {
            verification = flutterwaveService.verifyTransactionByReference(txRef);
        }

        if (verification.success() && "successful".equalsIgnoreCase(verification.status())) {
            BigDecimal paidAmount = verification.amount() != null ? verification.amount() : firstOrder.getAmount();
            for (EbookOrder order : orders) {
                if (!"completed".equalsIgnoreCase(order.getStatus())) {
                    completeOrder(order, verification.flwRef(), paidAmount);
                }
            }
            return EbookDto.EbookOrderResponse.from(firstOrder);
        } else {
            String paymentStatus = verification.status();
            for (EbookOrder order : orders) {
                if (!"completed".equalsIgnoreCase(order.getStatus())) {
                    order.setStatus("failed");
                    order.setFlutterwaveStatus(paymentStatus);
                    order.setFailedReason("Payment " + paymentStatus);
                    order.setFailedAt(LocalDateTime.now());
                    orderRepository.save(order);
                }
            }
            return EbookDto.EbookOrderResponse.from(firstOrder);
        }
    }

    public EbookDto.EbookOrderResponse completeOrderFromWebhook(String txRef, String flwRef,
                                                                 String status, BigDecimal amount) {
        List<EbookOrder> orders = orderRepository.findAllByTxRef(txRef);
        if (orders.isEmpty()) {
            logger.warn("Ebook order not found for webhook txRef={}", txRef);
            return null;
        }

        EbookOrder firstOrder = orders.get(0);

        if ("successful".equalsIgnoreCase(status)) {
            for (EbookOrder order : orders) {
                if (!"completed".equalsIgnoreCase(order.getStatus())) {
                    completeOrder(order, flwRef, amount);
                }
            }
            return EbookDto.EbookOrderResponse.from(firstOrder);
        } else {
            for (EbookOrder order : orders) {
                if (!"completed".equalsIgnoreCase(order.getStatus())) {
                    order.setFlwRef(flwRef);
                    order.setStatus("failed");
                    order.setFlutterwaveStatus(status);
                    order.setFailedReason("Payment " + status);
                    order.setFailedAt(LocalDateTime.now());
                    orderRepository.save(order);
                }
            }
            return EbookDto.EbookOrderResponse.from(firstOrder);
        }
    }

    private EbookDto.EbookOrderResponse completeOrder(EbookOrder order, String flwRef, BigDecimal amountPaid) {
        order.setFlwRef(flwRef);
        order.setAmountPaid(amountPaid);
        order.setStatus("completed");
        order.setFlutterwaveStatus("successful");
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);

        sendEbookDeliveryEmail(order);

        logger.info("Ebook order completed: txRef={}, buyer={}, ebook={}",
                order.getTxRef(), order.getBuyerEmail(), order.getEbook().getTitle());

        return EbookDto.EbookOrderResponse.from(order);
    }

    private void sendEbookDeliveryEmail(EbookOrder order) {
        try {
            String downloadUrl = order.getEbookVersion().getFileUrl();
            queueService.dispatch(JobType.EMAIL_EBOOK_DELIVERY, Map.of(
                    "to", order.getBuyerEmail(),
                    "subject", "Your TMAG Ebook is ready — " + order.getEbook().getTitle(),
                    "variables", Map.of(
                            "buyerName", order.getBuyerName(),
                            "ebookTitle", order.getEbook().getTitle(),
                            "versionLabel", order.getEbookVersion().getLabel(),
                            "downloadUrl", downloadUrl != null ? downloadUrl : "#",
                            "currencySymbol", order.getCurrencySymbol() != null ? order.getCurrencySymbol() : "",
                            "amount", order.getAmountPaid() != null ? order.getAmountPaid().toString() : order.getAmount().toString(),
                            "txRef", order.getTxRef()
                    )));
            order.setEmailSent(true);
            order.setEmailSentAt(LocalDateTime.now());
            orderRepository.save(order);
        } catch (Exception e) {
            logger.error("Failed to queue ebook delivery email for order txRef={}: {}", order.getTxRef(), e.getMessage());
        }
    }

    // ─── User: My ebooks ──────────────────────────────────────────────────────

    public List<EbookDto.EbookOrderResponse> getMyOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(o -> "completed".equalsIgnoreCase(o.getStatus()))
                .map(EbookDto.EbookOrderResponse::from)
                .toList();
    }

    public EbookDto.EbookOrderResponse getOrderByTxRef(String txRef) {
        return EbookDto.EbookOrderResponse.from(
                orderRepository.findByTxRef(txRef)
                        .orElseThrow(() -> new NoSuchElementException("Order not found")));
    }

    // ─── Admin: Ebook CRUD ────────────────────────────────────────────────────

    public List<EbookDto.AdminEbookResponse> listAllForAdmin() {
        return ebookRepository.findAll().stream()
                .map(e -> EbookDto.AdminEbookResponse.from(e,
                        versionRepository.findByEbookIdOrderBySortOrderAsc(e.getId())))
                .toList();
    }

    public EbookDto.AdminEbookResponse createEbook(EbookDto.CreateEbookRequest req) {
        if (ebookRepository.existsBySlug(req.slug())) {
            throw new IllegalArgumentException("Slug already in use");
        }
        Ebook ebook = new Ebook();
        applyEbookFields(ebook, req.title(), req.slug(), req.description(), req.shortDescription(),
                req.author(), req.authorBio(), req.coverUrl(), req.previewUrl(),
                req.pageCount(), req.publishedYear(), req.isbn(),
                req.isActive() != null ? req.isActive() : true,
                req.isFeatured() != null ? req.isFeatured() : false);
        ebookRepository.save(ebook);
        return EbookDto.AdminEbookResponse.from(ebook, List.of());
    }

    public EbookDto.AdminEbookResponse updateEbook(Long id, EbookDto.UpdateEbookRequest req) {
        Ebook ebook = ebookRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ebook not found"));
        applyEbookFields(ebook, req.title(), req.slug(), req.description(), req.shortDescription(),
                req.author(), req.authorBio(), req.coverUrl(), req.previewUrl(),
                req.pageCount(), req.publishedYear(), req.isbn(),
                req.isActive() != null ? req.isActive() : ebook.getIsActive(),
                req.isFeatured() != null ? req.isFeatured() : ebook.getIsFeatured());
        ebookRepository.save(ebook);
        return EbookDto.AdminEbookResponse.from(ebook,
                versionRepository.findByEbookIdOrderBySortOrderAsc(id));
    }

    public void deleteEbook(Long id) {
        Ebook ebook = ebookRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ebook not found"));
        ebookRepository.delete(ebook);
    }

    // ─── Admin: Version CRUD ──────────────────────────────────────────────────

    public EbookDto.AdminEbookVersionResponse addVersion(Long ebookId, EbookDto.CreateVersionRequest req) {
        Ebook ebook = ebookRepository.findById(ebookId)
                .orElseThrow(() -> new NoSuchElementException("Ebook not found"));
        EbookVersion version = new EbookVersion();
        version.setEbook(ebook);
        applyVersionFields(version, req.label(), req.countryCode(), req.countryName(), req.region(),
                req.price(), req.currency(), req.currencySymbol(), req.fileUrl(), req.fileKey(),
                req.fileSizeMb(), req.isActive() != null ? req.isActive() : true,
                req.sortOrder() != null ? req.sortOrder() : 0);
        versionRepository.save(version);
        return EbookDto.AdminEbookVersionResponse.from(version);
    }

    public EbookDto.AdminEbookVersionResponse updateVersion(Long ebookId, Long versionId, EbookDto.UpdateVersionRequest req) {
        EbookVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new NoSuchElementException("Version not found"));
        if (!version.getEbook().getId().equals(ebookId)) {
            throw new IllegalArgumentException("Version does not belong to this ebook");
        }
        applyVersionFields(version, req.label(), req.countryCode(), req.countryName(), req.region(),
                req.price(), req.currency(), req.currencySymbol(), req.fileUrl(), req.fileKey(),
                req.fileSizeMb(),
                req.isActive() != null ? req.isActive() : version.getIsActive(),
                req.sortOrder() != null ? req.sortOrder() : version.getSortOrder());
        versionRepository.save(version);
        return EbookDto.AdminEbookVersionResponse.from(version);
    }

    public void deleteVersion(Long ebookId, Long versionId) {
        EbookVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new NoSuchElementException("Version not found"));
        if (!version.getEbook().getId().equals(ebookId)) {
            throw new IllegalArgumentException("Version does not belong to this ebook");
        }
        versionRepository.delete(version);
    }

    // ─── Admin: Orders & Stats ────────────────────────────────────────────────

    public List<EbookDto.EbookOrderResponse> listAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(EbookDto.EbookOrderResponse::from)
                .toList();
    }

    public List<EbookDto.EbookOrderResponse> listOrdersByEbook(Long ebookId) {
        return orderRepository.findByEbookIdOrderByCreatedAtDesc(ebookId).stream()
                .map(EbookDto.EbookOrderResponse::from)
                .toList();
    }

    public EbookDto.EbookStatsResponse getStats() {
        return new EbookDto.EbookStatsResponse(
                orderRepository.count(),
                orderRepository.countAllCompleted(),
                orderRepository.sumTotalRevenue(),
                ebookRepository.count()
        );
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void applyEbookFields(Ebook e, String title, String slug, String description,
                                   String shortDescription, String author, String authorBio,
                                   String coverUrl, String previewUrl, Integer pageCount,
                                   Integer publishedYear, String isbn, Boolean isActive, Boolean isFeatured) {
        if (title != null) e.setTitle(title);
        if (slug != null) e.setSlug(slug);
        if (description != null) e.setDescription(description);
        if (shortDescription != null) e.setShortDescription(shortDescription);
        if (author != null) e.setAuthor(author);
        if (authorBio != null) e.setAuthorBio(authorBio);
        if (coverUrl != null) e.setCoverUrl(coverUrl);
        if (previewUrl != null) e.setPreviewUrl(previewUrl);
        if (pageCount != null) e.setPageCount(pageCount);
        if (publishedYear != null) e.setPublishedYear(publishedYear);
        if (isbn != null) e.setIsbn(isbn);
        e.setIsActive(isActive);
        e.setIsFeatured(isFeatured);
    }

    private void applyVersionFields(EbookVersion v, String label, String countryCode,
                                     String countryName, String region, BigDecimal price,
                                     String currency, String currencySymbol, String fileUrl,
                                     String fileKey, BigDecimal fileSizeMb, Boolean isActive, Integer sortOrder) {
        if (label != null) v.setLabel(label);
        if (countryCode != null) v.setCountryCode(countryCode);
        if (countryName != null) v.setCountryName(countryName);
        if (region != null) v.setRegion(region);
        if (price != null) v.setPrice(price);
        if (currency != null) v.setCurrency(currency);
        if (currencySymbol != null) v.setCurrencySymbol(currencySymbol);
        if (fileUrl != null) v.setFileUrl(fileUrl);
        if (fileKey != null) v.setFileKey(fileKey);
        if (fileSizeMb != null) v.setFileSizeMb(fileSizeMb);
        v.setIsActive(isActive);
        v.setSortOrder(sortOrder);
    }
}
