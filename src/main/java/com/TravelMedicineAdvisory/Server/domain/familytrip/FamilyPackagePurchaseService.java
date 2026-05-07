package com.TravelMedicineAdvisory.Server.domain.familytrip;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.config.CallbackRegistry;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwavePaymentRequest;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwavePaymentResponse;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwaveService;
import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import com.TravelMedicineAdvisory.Server.domain.familytrip.dto.FamilyPackageCheckoutRequest;
import com.TravelMedicineAdvisory.Server.domain.familytrip.dto.FamilyPackagePurchaseResponse;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;

@Service
@Transactional
public class FamilyPackagePurchaseService {

    private static final Logger logger = LoggerFactory.getLogger(FamilyPackagePurchaseService.class);

    private static final BigDecimal STANDARD_PRICE_USD = new BigDecimal("180"); // $180.00 in cents
    private static final BigDecimal STANDARD_PRICE_NGN = new BigDecimal("180000"); // ₦180,000 in kobo

    private final FamilyPackagePurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final FlutterwaveService flutterwaveService;
    private final CallbackRegistry callbackRegistry;
    private final PasswordEncoder passwordEncoder;
    private final QueueService queueService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public FamilyPackagePurchaseService(
            FamilyPackagePurchaseRepository purchaseRepository,
            UserRepository userRepository,
            FlutterwaveService flutterwaveService,
            CallbackRegistry callbackRegistry,
            PasswordEncoder passwordEncoder,
            QueueService queueService) {
        this.purchaseRepository = purchaseRepository;
        this.userRepository = userRepository;
        this.flutterwaveService = flutterwaveService;
        this.callbackRegistry = callbackRegistry;
        this.passwordEncoder = passwordEncoder;
        this.queueService = queueService;
    }

    public record CheckoutResult(
            String txRef,
            String paymentLink,
            String packageType,
            Integer tripsAllowed,
            BigDecimal amountMinor,
            String currency,
            String currencySymbol,
            Long purchaseId) {

    }

    public CheckoutResult initiateCheckout(Long userId, FamilyPackageCheckoutRequest request) {
        User user = null;
        String customerEmail;
        String customerName;
        String customerPhone;

        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
            customerEmail = user.getEmail();
            customerName = user.getName() != null ? user.getName() : user.getFirstName() + " " + user.getLastName();
            customerPhone = user.getPhone();
        } else {
            if (request.email() == null || request.email().isBlank()) {
                throw new IllegalArgumentException("Email is required for guest checkout");
            }
            customerEmail = request.email();
            customerName = request.name() != null ? request.name() : "Guest User";
            customerPhone = request.phone();
        }

        FamilyPackageType packageType = request.packageType();
        int tripsAllowed = 1;

        BillingCurrency currency = request.currency() != null ? request.currency() : BillingCurrency.USD;
        BigDecimal priceMinor;
        String currencySymbol = currency == BillingCurrency.NGN ? "₦" : "$";

        if (currency == BillingCurrency.NGN) {
            priceMinor = STANDARD_PRICE_NGN;
        } else {
            priceMinor = STANDARD_PRICE_USD;
        }

        String txRef = "TMAG_FAMILY_" + java.util.UUID.randomUUID().toString();

        FamilyPackagePurchase purchase = new FamilyPackagePurchase();
        purchase.setUser(user);
        purchase.setPackageType(packageType);
        purchase.setTripsAllowed(tripsAllowed);
        purchase.setTripsUsed(0);
        purchase.setAmountPaidMinor(priceMinor.longValue());
        purchase.setCurrency(currency.name());
        purchase.setStatus(FamilyPackagePurchaseStatus.PENDING);
        purchase.setTxRef(txRef);

        if (userId == null) {
            purchase.setGuestEmail(customerEmail);
            purchase.setGuestName(customerName);
            purchase.setGuestPhone(customerPhone);
        }

        purchaseRepository.save(purchase);

        logger.info("Initiating family package checkout: txRef={}, packageType={}, amount={}, guest={}",
                txRef, packageType, priceMinor, userId == null);

        String description = "TMAG Family Plan - One plan for up to 6 family members";

        FlutterwavePaymentRequest paymentRequest = new FlutterwavePaymentRequest(
                priceMinor,
                currency.name(),
                customerEmail,
                customerName,
                description,
                txRef,
                customerPhone,
                callbackRegistry.getBackendCallbackUrl("FAMILY_PACKAGE"),
                null,
                null,
                userId != null ? userId.toString() : "guest",
                null);

        FlutterwavePaymentResponse paymentResponse = flutterwaveService.initiatePayment(paymentRequest);

        if (paymentResponse.success() && paymentResponse.paymentLink() != null) {
            logger.info("Flutterwave payment initiated: txRef={}, paymentLink={}", txRef, paymentResponse.paymentLink());
            return new CheckoutResult(
                    txRef,
                    paymentResponse.paymentLink(),
                    packageType.name(),
                    tripsAllowed,
                    priceMinor,
                    currency.name(),
                    currencySymbol,
                    purchase.getId());
        } else {
            logger.error("Flutterwave payment initiation failed: txRef={}, error={}", txRef, paymentResponse.message());
            purchase.setStatus(FamilyPackagePurchaseStatus.REFUNDED);
            purchase.setFailedReason("Payment initiation failed: " + paymentResponse.message());
            purchaseRepository.save(purchase);
            throw new RuntimeException("Failed to initiate payment: " + paymentResponse.message());
        }
    }

    public FamilyPackagePurchaseResponse verifyAndCompletePurchase(String txRef, String transactionId) {
        FamilyPackagePurchase purchase = purchaseRepository.findByTxRef(txRef)
                .orElseThrow(() -> new NoSuchElementException("Purchase not found with txRef: " + txRef));


        if (purchase.getStatus() == FamilyPackagePurchaseStatus.ACTIVE) {
            return FamilyPackagePurchaseResponse.from(purchase);
        }

        FlutterwavePaymentResponse verification;
        if (transactionId != null && !transactionId.isBlank()) {
            logger.info("Verifying family package payment by transaction ID: {}", transactionId);
            verification = flutterwaveService.verifyTransaction(transactionId);
        } else {
            logger.info("Verifying family package payment by tx_ref: {}", txRef);
            verification = flutterwaveService.verifyTransactionByReference(txRef);
        }

        if (verification.success() && "successful".equalsIgnoreCase(verification.status())) {
            return completePurchase(purchase, verification);
        } else {
            String status = verification.status();
            purchase.setStatus(FamilyPackagePurchaseStatus.REFUNDED);
            purchase.setFlutterwaveStatus(status);
            purchase.setFailedReason("Payment " + status);
            purchaseRepository.save(purchase);
            logger.info("Family package payment not successful: txRef={}, status={}", txRef, status);
            return FamilyPackagePurchaseResponse.from(purchase);
        }
    }

    private FamilyPackagePurchaseResponse completePurchase(FamilyPackagePurchase purchase,
            FlutterwavePaymentResponse verification) {
        // Link guest purchase to user on activation
        if (purchase.getUser() == null && purchase.getGuestEmail() != null) {
            User user = findOrCreateUser(purchase.getGuestEmail(), purchase.getGuestName(), purchase.getGuestPhone());
            purchase.setUser(user);
            logger.info("Linked guest family purchase to user: email={}, userId={}", purchase.getGuestEmail(), user.getId());
        }

        purchase.setStatus(FamilyPackagePurchaseStatus.ACTIVE);
        purchase.setPaymentProvider("FLUTTERWAVE");
        purchase.setPaymentReference(verification.flwRef() != null ? verification.flwRef() : verification.txRef());
        purchase.setPaidAt(LocalDateTime.now());
        purchase.setFlutterwaveStatus(verification.status());

        purchase.setExpiresAt(null);

        purchaseRepository.save(purchase);
        logger.info("Family package purchase completed: txRef={}, packageType={}", purchase.getTxRef(),
                purchase.getPackageType());
        return FamilyPackagePurchaseResponse.from(purchase);
    }

    public FamilyPackagePurchaseResponse completePurchaseFromWebhook(String txRef, String flwRef, String status,
            Long amountMinor) {
        FamilyPackagePurchase purchase = purchaseRepository.findByTxRef(txRef).orElse(null);
        if (purchase == null) {
            logger.warn("Family package purchase not found for webhook: txRef={}", txRef);
            return null;
        }

        if (purchase.getStatus() == FamilyPackagePurchaseStatus.ACTIVE) {
            return FamilyPackagePurchaseResponse.from(purchase);
        }

        if ("successful".equalsIgnoreCase(status)) {
            // Link guest purchase to user on activation
            if (purchase.getUser() == null && purchase.getGuestEmail() != null) {
                User user = findOrCreateUser(purchase.getGuestEmail(), purchase.getGuestName(), purchase.getGuestPhone());
                purchase.setUser(user);
                logger.info("Linked guest family purchase to user (webhook): email={}, userId={}", purchase.getGuestEmail(),
                        user.getId());
            }

            purchase.setStatus(FamilyPackagePurchaseStatus.ACTIVE);
            purchase.setPaymentProvider("FLUTTERWAVE");
            purchase.setPaymentReference(flwRef);
            purchase.setPaidAt(LocalDateTime.now());
            purchase.setFlutterwaveStatus(status);
            purchase.setAmountPaidMinor(amountMinor != null ? amountMinor : purchase.getAmountPaidMinor());

            purchase.setExpiresAt(null);

            purchaseRepository.save(purchase);
            logger.info("Family package purchase completed from webhook: txRef={}", txRef);
            return FamilyPackagePurchaseResponse.from(purchase);
        } else {
            purchase.setStatus(FamilyPackagePurchaseStatus.REFUNDED);
            purchase.setPaymentProvider("FLUTTERWAVE");
            purchase.setFlutterwaveStatus(status);
            purchaseRepository.save(purchase);
            return FamilyPackagePurchaseResponse.from(purchase);
        }
    }

    public Optional<FamilyPackagePurchaseResponse> getActivePurchase(Long userId) {
        return purchaseRepository.findActiveByUserId(userId)
                .map(FamilyPackagePurchaseResponse::from);
    }

    public List<FamilyPackagePurchaseResponse> getPurchaseHistory(Long userId) {
        return purchaseRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(FamilyPackagePurchaseResponse::from)
                .toList();
    }

    private User findOrCreateUser(String email, String guestName, String guestPhone) {
        Optional<User> existing = userRepository.findByEmailIgnoreCase(email);
        if (existing.isPresent()) {
            return existing.get();
        }

        String[] nameParts = guestName != null && guestName.contains(" ")
                ? guestName.split(" ", 2)
                : new String[]{guestName != null ? guestName : "Guest", ""};

        String username = email.split("@")[0];
        String baseUsername = username;
        int suffix = 1;
        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + suffix++;
        }

        String rawPassword = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User user = new User();
        user.setFirstName(nameParts[0]);
        user.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        user.setName(guestName);
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setPhone(guestPhone);
        user.setType("FAMILY");
        user.setOnboardingStage(5);
        user.setOnboarded(false);
        user.setVerified(false);
        user.setIsActive(true);

        String resetToken = generateRandomToken(32);
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);

        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken + "&email=" + encodedEmail;
        queueService.dispatch(JobType.EMAIL_FAMILY_PLAN_WELCOME, Map.of(
                "to", email,
                "subject", "Welcome to your TMAG Family Plan",
                "variables", Map.of(
                        "firstName", user.getFirstName() != null ? user.getFirstName() : "there",
                        "link", resetLink)));
        return user;
    }

    private String generateRandomPassword() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateRandomToken(int byteLength) {
        byte[] bytes = new byte[byteLength];
        new SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
