package com.TravelMedicineAdvisory.Server.domain.creditpurchase;

import com.TravelMedicineAdvisory.Server.core.currency.ExchangeRateService;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwavePaymentRequest;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwavePaymentResponse;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwaveService;
import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import com.TravelMedicineAdvisory.Server.domain.credit.Credit;
import com.TravelMedicineAdvisory.Server.domain.credit.CreditRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.TravelMedicineAdvisory.Server.config.CallbackRegistry;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlan;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanCode;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class CreditPurchaseService {

    private static final Logger logger = LoggerFactory.getLogger(CreditPurchaseService.class);

    private final CreditPurchaseRepository purchaseRepository;
    private final CreditRepository creditRepository;
    private final UserRepository userRepository;
    private final FlutterwaveService flutterwaveService;
    private final QueueService queueService;
    private final ExchangeRateService exchangeRateService;
    private final CreditPlanRepository userCreditPlanRepository;
    private final CallbackRegistry callbackRegistry;

    public CreditPurchaseService(
            CreditPurchaseRepository purchaseRepository,
            CreditRepository creditRepository,
            UserRepository userRepository,
            FlutterwaveService flutterwaveService,
            QueueService queueService,
            ExchangeRateService exchangeRateService,
            CreditPlanRepository userCreditPlanRepository,
            CallbackRegistry callbackRegistry) {
        this.purchaseRepository = purchaseRepository;
        this.creditRepository = creditRepository;
        this.userRepository = userRepository;
        this.flutterwaveService = flutterwaveService;
        this.queueService = queueService;
        this.exchangeRateService = exchangeRateService;
        this.userCreditPlanRepository = userCreditPlanRepository;
        this.callbackRegistry = callbackRegistry;
    }

    public record PurchaseInitiationResult(
            String txRef,
            String paymentLink,
            Integer credits,
            BigDecimal basePrice,
            BigDecimal discountAmount,
            BigDecimal totalAmount,
            BillingCurrency currency,
            String currencySymbol,
            BigDecimal pricePerCredit,
            Long purchaseId) {
    }

    public PurchaseInitiationResult initiatePurchase(Long userId, CreditPurchaseRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if ("COMPANY".equalsIgnoreCase(user.getType())) {
            throw new IllegalStateException(
                    "Company users cannot purchase credits directly. Please contact your HR department.");
        }

        // Resolve the user's credit plan; fall back to STANDARD if none assigned
        CreditPlan creditPlan = user.getCreditPlan();
        if (creditPlan == null) {
            creditPlan = userCreditPlanRepository.findByIsDefaultTrue()
                    .orElseGet(() -> userCreditPlanRepository.findByCode(CreditPlanCode.STANDARD).orElse(null));
        }
        if (creditPlan == null) {
            throw new IllegalStateException("No pricing plan assigned to user");
        }

        BillingCurrency currency = user.getBillingCurrency() != null ? user.getBillingCurrency() : BillingCurrency.USD;
        String currencyCode = currency.name();
        String currencySymbol = exchangeRateService.getCurrencySymbol(currencyCode);
        BigDecimal pricePerCredit = BigDecimal.ZERO;

        if (currencyCode.equals("NGN") && creditPlan.getBasePriceNgn() != null) {
            pricePerCredit = creditPlan.getBasePriceNgn();
        } else {
            pricePerCredit = creditPlan.getBasePriceUsd();
        }

        BigDecimal totalAmount = pricePerCredit.multiply(BigDecimal.valueOf(request.credits()));

        String txRef = flutterwaveService.generateTransactionReference();

        CreditPurchase purchase = new CreditPurchase();
        purchase.setTxRef(txRef);
        purchase.setUser(user);
        purchase.setCreditsPurchased(request.credits());
        purchase.setCurrency(currency);
        purchase.setCurrencySymbol(currencySymbol);
        purchase.setPricePerCredit(pricePerCredit);
        purchase.setAmount(totalAmount);
        purchase.setStatus("pending");
        purchaseRepository.save(purchase);

        logger.info("Initiating Flutterwave payment for txRef={}, credits={}, amount={}",
                txRef, request.credits(), totalAmount);

        FlutterwavePaymentRequest paymentRequest = new FlutterwavePaymentRequest(
                totalAmount,
                currencyCode,
                user.getEmail(),
                user.getName() != null ? user.getName() : user.getFirstName() + " " + user.getLastName(),
                "TMAG Credit Purchase - " + request.credits() + " credits",
                txRef,
                user.getPhone(),
                callbackRegistry.getBackendCallbackUrl("CREDIT_PURCHASE"),
                request.credits(),
                null,
                userId.toString(),
                null);

        FlutterwavePaymentResponse paymentResponse = flutterwaveService.initiatePayment(paymentRequest);

        if (paymentResponse.success() && paymentResponse.paymentLink() != null) {
            logger.info("Flutterwave payment initiated successfully. txRef={}, paymentLink={}",
                    txRef, paymentResponse.paymentLink());
            return new PurchaseInitiationResult(
                    txRef,
                    paymentResponse.paymentLink(),
                    request.credits(),
                    totalAmount,
                    BigDecimal.ZERO,
                    totalAmount,
                    currency,
                    currencySymbol,
                    pricePerCredit,
                    purchase.getId());
        } else {
            logger.error("Flutterwave payment initiation failed. txRef={}, error={}",
                    txRef, paymentResponse.message());
            purchase.setStatus("failed");
            purchase.setFailedReason("Payment initiation failed: " + paymentResponse.message());
            purchase.setFailedAt(LocalDateTime.now());
            purchaseRepository.save(purchase);
            throw new RuntimeException("Failed to initiate payment: " + paymentResponse.message());
        }
    }

    public CreditPurchaseResponse verifyAndCompletePurchase(String txRef, String transactionId) {
        CreditPurchase purchase = purchaseRepository.findByTxRef(txRef)
                .orElseThrow(() -> new NoSuchElementException("Purchase not found with txRef: " + txRef));

        if ("completed".equalsIgnoreCase(purchase.getStatus())) {
            return CreditPurchaseResponse.from(purchase);
        }

        if (purchase.getCompanyId() != null) {
            logger.info("Skipping company purchase in user verify handler: txRef={}, companyId={}", txRef,
                    purchase.getCompanyId());
            return CreditPurchaseResponse.from(purchase);
        }

        // Use the Flutterwave transaction ID for verification when available
        // (preferred),
        // as GET /transactions/{id}/verify returns a single object.
        // The tx_ref search endpoint returns an array which may be empty due to
        // indexing delay.
        FlutterwavePaymentResponse verification;
        if (transactionId != null && !transactionId.isBlank()) {
            logger.info("Verifying payment by transaction ID: {}", transactionId);
            verification = flutterwaveService.verifyTransaction(transactionId);
        } else {
            logger.info("Verifying payment by tx_ref: {}", txRef);
            verification = flutterwaveService.verifyTransactionByReference(txRef);
        }

        // Check both API-level success AND payment-level status.
        // Flutterwave returns status:"success" at API level even for cancelled/failed
        // payments —
        // the actual payment outcome is in data.status ("successful", "failed",
        // "cancelled").
        if (verification.success() && "successful".equalsIgnoreCase(verification.status())) {
            return completePurchase(purchase, verification);
        } else {
            String paymentStatus = verification.status();
            String reason = paymentStatus != null
                    ? "Payment " + paymentStatus
                    : "Verification failed: " + verification.message();
            purchase.setStatus("failed");
            purchase.setFlutterwaveStatus(paymentStatus);
            purchase.setFailedReason(reason);
            purchase.setFailedAt(LocalDateTime.now());
            purchaseRepository.save(purchase);
            logger.info("Payment verification not successful: txRef={}, flutterwaveStatus={}", txRef, paymentStatus);
            return CreditPurchaseResponse.from(purchase);
        }
    }

    public CreditPurchaseResponse completePurchase(String txRef, String flwRef, BigDecimal amountPaid) {
        CreditPurchase purchase = purchaseRepository.findByTxRef(txRef)
                .orElseThrow(() -> new NoSuchElementException("Purchase not found with txRef: " + txRef));

        if ("completed".equalsIgnoreCase(purchase.getStatus())) {
            return CreditPurchaseResponse.from(purchase);
        }

        if (purchase.getCompanyId() != null) {
            logger.info("Skipping company purchase in user complete handler: txRef={}, companyId={}", txRef,
                    purchase.getCompanyId());
            return CreditPurchaseResponse.from(purchase);
        }

        if (creditRepository.existsByTypeAndReference("purchase", txRef)) {
            logger.info("Credit entry already exists for txRef={}, skipping duplicate", txRef);
            purchase.setStatus("completed");
            purchase.setFlwRef(flwRef);
            purchase.setAmountPaid(amountPaid);
            purchase.setPaidAt(LocalDateTime.now());
            purchase.setFlutterwaveStatus("successful");
            purchaseRepository.save(purchase);
            return CreditPurchaseResponse.from(purchase);
        }

        purchase.setFlwRef(flwRef);
        purchase.setAmountPaid(amountPaid);
        purchase.setStatus("completed");
        purchase.setPaidAt(LocalDateTime.now());
        purchase.setFlutterwaveStatus("successful");
        purchaseRepository.save(purchase);

        User user = purchase.getUser();
        user.setCredits(user.getCredits() + purchase.getCreditsPurchased());
        userRepository.save(user);

        Credit creditEntry = new Credit();
        creditEntry.setUser(user);
        creditEntry.setAmount(purchase.getCreditsPurchased());
        creditEntry.setType("purchase");
        creditEntry.setReference(txRef);
        creditEntry.setBalanceAfter(user.getCredits());
        creditRepository.save(creditEntry);

        logger.info("Credit purchase completed: txRef={}, credits={}, userId={}",
                txRef, purchase.getCreditsPurchased(), user.getId());

        return CreditPurchaseResponse.from(purchase);
    }

    public CreditPurchaseResponse completePurchaseFromWebhook(String txRef, String flwRef, String status,
            BigDecimal amount) {
        CreditPurchase purchase = purchaseRepository.findByTxRef(txRef)
                .orElse(null);

        if (purchase == null) {
            logger.warn("Purchase not found for webhook: txRef={}", txRef);
            return null;
        }

        if ("completed".equalsIgnoreCase(purchase.getStatus())) {
            return CreditPurchaseResponse.from(purchase);
        }

        if (purchase.getCompanyId() != null) {
            logger.info("Skipping company purchase in user webhook handler: txRef={}, companyId={}", txRef,
                    purchase.getCompanyId());
            return CreditPurchaseResponse.from(purchase);
        }

        if ("successful".equalsIgnoreCase(status)) {
            return completePurchase(purchase, new FlutterwavePaymentResponse(
                    true, "Payment successful", txRef, flwRef, status, null, amount,
                    purchase.getCurrency().name(), null, null, purchase.getId()));
        } else {
            purchase.setFlwRef(flwRef);
            purchase.setStatus("failed");
            purchase.setFlutterwaveStatus(status);
            purchase.setFailedReason("Payment " + status);
            purchase.setFailedAt(LocalDateTime.now());
            purchaseRepository.save(purchase);
            return CreditPurchaseResponse.from(purchase);
        }
    }

    private CreditPurchaseResponse completePurchase(CreditPurchase purchase, FlutterwavePaymentResponse verification) {
        purchase.setStatus("completed");
        purchase.setPaidAt(LocalDateTime.now());
        purchase.setFlutterwaveStatus(verification.status());
        purchaseRepository.save(purchase);

        if (creditRepository.existsByTypeAndReference("purchase", purchase.getTxRef())) {
            logger.info("Credit entry already exists for txRef={}, skipping duplicate", purchase.getTxRef());
            return CreditPurchaseResponse.from(purchase);
        }

        User user = purchase.getUser();
        user.setCredits(user.getCredits() + purchase.getCreditsPurchased());
        userRepository.save(user);

        Credit creditEntry = new Credit();
        creditEntry.setUser(user);
        creditEntry.setAmount(purchase.getCreditsPurchased());
        creditEntry.setType("purchase");
        creditEntry.setReference(purchase.getTxRef());
        creditEntry.setBalanceAfter(user.getCredits());
        creditRepository.save(creditEntry);

        logger.info("Credit purchase completed: txRef={}, credits={}, userId={}",
                purchase.getTxRef(), purchase.getCreditsPurchased(), user.getId());

        String firstName = user.getFirstName() != null ? user.getFirstName() : "there";
        queueService.dispatch(JobType.EMAIL_CREDIT_PURCHASE, Map.of(
                "to", user.getEmail(),
                "subject", "Your TMAG credit purchase is complete",
                "variables", Map.of(
                        "firstName", firstName,
                        "credits", String.valueOf(purchase.getCreditsPurchased()),
                        "currencySymbol", purchase.getCurrencySymbol(),
                        "amount", purchase.getAmount().toString())));

        return CreditPurchaseResponse.from(purchase);
    }

    public List<CreditPurchaseResponse> getPurchaseHistory(Long userId) {
        return purchaseRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(CreditPurchaseResponse::from)
                .toList();
    }

    public CreditPurchaseResponse getPurchaseByTxRef(String txRef) {
        CreditPurchase purchase = purchaseRepository.findByTxRef(txRef)
                .orElseThrow(() -> new NoSuchElementException("Purchase not found"));
        return CreditPurchaseResponse.from(purchase);
    }

    public Optional<CreditPurchaseResponse> findPendingPurchase(Long userId, String txRef) {
        return purchaseRepository.findByUserIdAndTxRef(userId, txRef)
                .map(CreditPurchaseResponse::from);
    }
}
