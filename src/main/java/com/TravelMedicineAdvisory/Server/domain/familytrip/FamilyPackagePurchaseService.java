package com.TravelMedicineAdvisory.Server.domain.familytrip;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.config.CallbackRegistry;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwavePaymentRequest;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwavePaymentResponse;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwaveService;
import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import com.TravelMedicineAdvisory.Server.domain.familytrip.dto.FamilyPackageCheckoutRequest;
import com.TravelMedicineAdvisory.Server.domain.familytrip.dto.FamilyPackagePurchaseResponse;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;

@Service
@Transactional
public class FamilyPackagePurchaseService {

    private static final Logger logger = LoggerFactory.getLogger(FamilyPackagePurchaseService.class);

    private static final BigDecimal ONE_TRIP_PRICE_USD = new BigDecimal("1800"); // $180.00 in cents
    private static final BigDecimal TWO_TRIP_PRICE_USD = new BigDecimal("3600"); // $360.00 in cents
    private static final BigDecimal ONE_TRIP_PRICE_NGN = new BigDecimal("180000"); // ₦180,000 in kobo
    private static final BigDecimal TWO_TRIP_PRICE_NGN = new BigDecimal("360000"); // ₦360,000 in kobo

    private final FamilyPackagePurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final FlutterwaveService flutterwaveService;
    private final CallbackRegistry callbackRegistry;

    public FamilyPackagePurchaseService(
            FamilyPackagePurchaseRepository purchaseRepository,
            UserRepository userRepository,
            FlutterwaveService flutterwaveService,
            CallbackRegistry callbackRegistry) {
        this.purchaseRepository = purchaseRepository;
        this.userRepository = userRepository;
        this.flutterwaveService = flutterwaveService;
        this.callbackRegistry = callbackRegistry;
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        FamilyPackageType packageType = request.packageType();
        int tripsAllowed = packageType == FamilyPackageType.ONE_TRIP ? 1 : 2;

        BillingCurrency currency = request.currency() != null ? request.currency() : BillingCurrency.USD;
        BigDecimal priceMinor;
        String currencySymbol = currency == BillingCurrency.NGN ? "₦" : "$";

        if (currency == BillingCurrency.NGN) {
            priceMinor = packageType == FamilyPackageType.ONE_TRIP ? ONE_TRIP_PRICE_NGN : TWO_TRIP_PRICE_NGN;
        } else {
            priceMinor = packageType == FamilyPackageType.ONE_TRIP ? ONE_TRIP_PRICE_USD : TWO_TRIP_PRICE_USD;
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
        purchaseRepository.save(purchase);

        logger.info("Initiating family package checkout: txRef={}, packageType={}, amount={}",
                txRef, packageType, priceMinor);

        String description = packageType == FamilyPackageType.ONE_TRIP
                ? "TMAG Family Plan - One plan for up to 6 family members"
                : "TMAG Family Plan - Two standalone family plans";

        FlutterwavePaymentRequest paymentRequest = new FlutterwavePaymentRequest(
                priceMinor,
                currency.name(),
                user.getEmail(),
                user.getName() != null ? user.getName() : user.getFirstName() + " " + user.getLastName(),
                description,
                txRef,
                user.getPhone(),
                callbackRegistry.getBackendCallbackUrl("FAMILY_PACKAGE"),
                null,
                null,
                userId.toString(),
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

    private FamilyPackagePurchaseResponse completePurchase(FamilyPackagePurchase purchase, FlutterwavePaymentResponse verification) {
        purchase.setStatus(FamilyPackagePurchaseStatus.ACTIVE);
        purchase.setPaymentProvider("FLUTTERWAVE");
        purchase.setPaymentReference(verification.flwRef() != null ? verification.flwRef() : verification.txRef());
        purchase.setPaidAt(LocalDateTime.now());
        purchase.setFlutterwaveStatus(verification.status());

        purchase.setExpiresAt(null);

        purchaseRepository.save(purchase);
        logger.info("Family package purchase completed: txRef={}, packageType={}", purchase.getTxRef(), purchase.getPackageType());
        return FamilyPackagePurchaseResponse.from(purchase);
    }

    public FamilyPackagePurchaseResponse completePurchaseFromWebhook(String txRef, String flwRef, String status, Long amountMinor) {
        FamilyPackagePurchase purchase = purchaseRepository.findByTxRef(txRef).orElse(null);
        if (purchase == null) {
            logger.warn("Family package purchase not found for webhook: txRef={}", txRef);
            return null;
        }

        if (purchase.getStatus() == FamilyPackagePurchaseStatus.ACTIVE) {
            return FamilyPackagePurchaseResponse.from(purchase);
        }

        if ("successful".equalsIgnoreCase(status)) {
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
}
