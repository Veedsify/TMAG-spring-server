package com.TravelMedicineAdvisory.Server.domain.ebook;

import com.TravelMedicineAdvisory.Server.core.currency.ExchangeRateService;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwavePaymentRequest;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwavePaymentResponse;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwaveService;
import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.TravelMedicineAdvisory.Server.security.AppUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1")
public class CartItemController {

    private final CartItemService cartItemService;
    private final EbookVersionRepository versionRepository;
    private final EbookOrderRepository orderRepository;
    private final UserRepository userRepository;
    private final FlutterwaveService flutterwaveService;
    private final QueueService queueService;
    private final ExchangeRateService exchangeRateService;

    private static final String EBOOK_CALLBACK_URL_KEY = "${app.payment.flutterwave.ebook-callback-url:http://localhost:3000/shop/order-confirmation}";

    @org.springframework.beans.factory.annotation.Value(EBOOK_CALLBACK_URL_KEY)
    private String ebookCallbackUrl;

    public CartItemController(CartItemService cartItemService,
                              EbookVersionRepository versionRepository,
                              EbookOrderRepository orderRepository,
                              UserRepository userRepository,
                              FlutterwaveService flutterwaveService,
                              QueueService queueService,
                              ExchangeRateService exchangeRateService) {
        this.cartItemService = cartItemService;
        this.versionRepository = versionRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.flutterwaveService = flutterwaveService;
        this.queueService = queueService;
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping("/cart")
    public ResponseEntity<SuccessResponse> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(new SuccessResponse("Cart retrieved", cartItemService.getCart(userId)));
    }

    @PostMapping("/cart/add")
    public ResponseEntity<SuccessResponse> addToCart(
            @RequestBody Map<String, Long> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = requireUserId(userDetails);
        Long ebookVersionId = body.get("ebookVersionId");
        if (ebookVersionId == null) {
            return ResponseEntity.badRequest().body(new SuccessResponse("ebookVersionId is required", null));
        }
        try {
            return ResponseEntity.ok(new SuccessResponse("Added to cart", cartItemService.addToCart(userId, ebookVersionId)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/cart/{cartItemId}")
    public ResponseEntity<SuccessResponse> removeFromCart(
            @PathVariable Long cartItemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = requireUserId(userDetails);
        try {
            cartItemService.removeFromCart(userId, cartItemId);
            return ResponseEntity.ok(new SuccessResponse("Removed from cart", cartItemService.getCart(userId)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/cart")
    public ResponseEntity<SuccessResponse> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = requireUserId(userDetails);
        cartItemService.clearCart(userId);
        return ResponseEntity.ok(new SuccessResponse("Cart cleared", List.of()));
    }

    @PostMapping("/cart/sync")
    public ResponseEntity<SuccessResponse> syncCart(
            @RequestBody List<EbookDto.CartSyncItem> items,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(new SuccessResponse("Cart synced", cartItemService.syncCart(userId, items)));
    }

    @PostMapping("/cart/checkout")
    public ResponseEntity<SuccessResponse> cartCheckout(
            @RequestBody EbookDto.CartCheckoutRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);

        if (request.ebookVersionIds() == null || request.ebookVersionIds().isEmpty()) {
            return ResponseEntity.badRequest().body(new SuccessResponse("At least one item is required", null));
        }

        try {
            User user = null;
            String buyerEmail;
            String buyerName;
            String buyerPhone;

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
            List<Long> orderIds = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            String currency = null;
            String currencySymbol = null;
            List<String> itemLabels = new ArrayList<>();

            for (Long versionId : request.ebookVersionIds()) {
                EbookVersion version = versionRepository.findById(versionId).orElse(null);
                if (version == null || !Boolean.TRUE.equals(version.getIsActive())) {
                    continue;
                }
                if (!Boolean.TRUE.equals(version.getEbook().getIsActive())) {
                    continue;
                }

                // Use the first item's currency as the payment currency
                if (currency == null) {
                    currency = version.getCurrency();
                    currencySymbol = version.getCurrencySymbol();
                }

                // Convert price to payment currency if different
                BigDecimal itemAmount = version.getPrice();
                if (!version.getCurrency().equalsIgnoreCase(currency)) {
                    itemAmount = exchangeRateService.convert(version.getPrice(), version.getCurrency(), currency);
                }

                EbookOrder order = new EbookOrder();
                order.setTxRef(txRef);
                order.setEbook(version.getEbook());
                order.setEbookVersion(version);
                order.setAmount(itemAmount);
                order.setCurrency(currency);
                order.setCurrencySymbol(currencySymbol);
                order.setStatus("pending");

                if (user != null) {
                    order.setUser(user);
                } else {
                    order.setGuestEmail(buyerEmail);
                    order.setGuestName(buyerName);
                    order.setGuestPhone(buyerPhone);
                }

                orderRepository.save(order);
                orderIds.add(order.getId());
                totalAmount = totalAmount.add(itemAmount);
                itemLabels.add(version.getEbook().getTitle() + " — " + version.getLabel());
            }

            if (orderIds.isEmpty()) {
                return ResponseEntity.badRequest().body(new SuccessResponse("No valid items to checkout", null));
            }

            String description = orderIds.size() == 1
                    ? "TMAG Ebook: " + itemLabels.get(0)
                    : "TMAG Ebooks (" + orderIds.size() + " items)";

            String callbackWithRef = ebookCallbackUrl + "?txRef=" + txRef;

            FlutterwavePaymentRequest paymentRequest = new FlutterwavePaymentRequest(
                    totalAmount,
                    currency,
                    buyerEmail,
                    buyerName,
                    description,
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
                for (Long orderId : orderIds) {
                    orderRepository.findById(orderId).ifPresent(o -> {
                        o.setStatus("failed");
                        o.setFailedReason("Payment initiation failed: " + paymentResponse.message());
                        o.setFailedAt(LocalDateTime.now());
                        orderRepository.save(o);
                    });
                }
                throw new RuntimeException("Failed to initiate payment: " + paymentResponse.message());
            }

            // Clear the user's cart after successful payment initiation
            if (userId != null) {
                cartItemService.clearCart(userId);
            }

            return ResponseEntity.ok(new SuccessResponse("Checkout initiated", new EbookDto.CartCheckoutResponse(
                    txRef, paymentResponse.paymentLink(), orderIds,
                    totalAmount, currency, currencySymbol, orderIds.size()
            )));
        } catch (NoSuchElementException | IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse(e.getMessage(), null));
        }
    }

    private Long resolveUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        if (userDetails instanceof AppUserDetails appUserDetails) {
            return appUserDetails.getUserId();
        }
        return userRepository.findByEmail(userDetails.getUsername())
                .map(u -> u.getId()).orElse(null);
    }

    private Long requireUserId(UserDetails userDetails) {
        Long id = resolveUserId(userDetails);
        if (id == null) throw new IllegalStateException("Authentication required");
        return id;
    }
}
