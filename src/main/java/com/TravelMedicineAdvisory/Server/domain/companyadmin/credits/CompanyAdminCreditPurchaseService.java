package com.TravelMedicineAdvisory.Server.domain.companyadmin.credits;

import com.TravelMedicineAdvisory.Server.config.CallbackRegistry;
import com.TravelMedicineAdvisory.Server.core.cache.CacheNames;
import com.TravelMedicineAdvisory.Server.core.currency.ExchangeRateService;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwavePaymentRequest;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwavePaymentResponse;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwaveService;
import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.companysetting.CompanySettingRepository;
import com.TravelMedicineAdvisory.Server.domain.credit.Credit;
import com.TravelMedicineAdvisory.Server.domain.credit.CreditRepository;
import com.TravelMedicineAdvisory.Server.domain.creditpurchase.CreditPurchase;
import com.TravelMedicineAdvisory.Server.domain.creditpurchase.CreditPurchaseRepository;
import com.TravelMedicineAdvisory.Server.domain.creditpurchase.CreditPurchaseResponse;
import com.TravelMedicineAdvisory.Server.domain.invoice.Invoice;
import com.TravelMedicineAdvisory.Server.domain.invoice.InvoiceRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlan;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class CompanyAdminCreditPurchaseService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyAdminCreditPurchaseService.class);

    private static final int TIER_1_MAX = 49;
    private static final int TIER_2_MAX = 99;
    private static final int TIER_3_MAX = 499;

    private static final BigDecimal TIER_1_MULTIPLIER = BigDecimal.ONE;
    private static final BigDecimal TIER_2_MULTIPLIER = new BigDecimal("0.90");
    private static final BigDecimal TIER_3_MULTIPLIER = new BigDecimal("0.80");

    private final CompanyRepository companyRepository;
    private final CompanySettingRepository settingRepository;
    private final CreditRepository creditRepository;
    private final UserRepository userRepository;
    private final CreditPurchaseRepository purchaseRepository;
    private final FlutterwaveService flutterwaveService;
    private final InvoiceRepository invoiceRepository;
    private final ExchangeRateService exchangeRateService;
    private final CreditPlanRepository userCreditPlanRepository;
    private final CacheManager cacheManager;
    private final CallbackRegistry callbackRegistry;

    public CompanyAdminCreditPurchaseService(
            CompanyRepository companyRepository,
            CompanySettingRepository settingRepository,
            CreditRepository creditRepository,
            UserRepository userRepository,
            CreditPurchaseRepository purchaseRepository,
            FlutterwaveService flutterwaveService,
            InvoiceRepository invoiceRepository,
            ExchangeRateService exchangeRateService,
            CreditPlanRepository userCreditPlanRepository,
            CacheManager cacheManager,
            CallbackRegistry callbackRegistry) {
        this.companyRepository = companyRepository;
        this.settingRepository = settingRepository;
        this.creditRepository = creditRepository;
        this.userRepository = userRepository;
        this.purchaseRepository = purchaseRepository;
        this.flutterwaveService = flutterwaveService;
        this.invoiceRepository = invoiceRepository;
        this.exchangeRateService = exchangeRateService;
        this.userCreditPlanRepository = userCreditPlanRepository;
        this.cacheManager = cacheManager;
        this.callbackRegistry = callbackRegistry;
    }

    public record CompanyPricingResult(
            Long companyId,
            String companyName,
            BillingCurrency currency,
            String currencySymbol,
            BigDecimal pricePerCredit,
            String appliedTier,
            boolean qualifiesForContactSales,
            Integer historicalCreditsPurchased,
            BigDecimal basePricePerCreditUsd,
            BigDecimal pricePerCreditTier1,
            BigDecimal pricePerCreditTier2,
            BigDecimal pricePerCreditTier3,
            Integer tier1MaxCredits,
            Integer tier2MaxCredits,
            Integer tier3MaxCredits) {
    }

    @Cacheable(cacheNames = CacheNames.COMPANY_ADMIN_CREDITS_PRICING, key = "#companyId")
    @Transactional(readOnly = true)
    public CompanyPricingResult getCompanyPricing(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));

        BillingCurrency currency = resolveBillingCurrency(company);
        CreditPlan plan = resolveCreditPlan(company);
        int historicalPurchased = company.getHistoricalCreditsPurchased();

        BigDecimal baseUsd = plan.getBasePriceUsd();
        String currencySymbol = exchangeRateService.getCurrencySymbol(currency.name());

        TierPricing tier = computeTierPricing(historicalPurchased, baseUsd, currency, currencySymbol);

        return new CompanyPricingResult(
                company.getId(),
                company.getName(),
                currency,
                currencySymbol,
                tier.pricePerCredit(),
                tier.appliedTier(),
                tier.qualifiesForContactSales(),
                historicalPurchased,
                baseUsd,
                exchangeRateService.convertFromUsd(baseUsd.multiply(TIER_1_MULTIPLIER), currency.name()),
                exchangeRateService.convertFromUsd(baseUsd.multiply(TIER_2_MULTIPLIER), currency.name()),
                exchangeRateService.convertFromUsd(baseUsd.multiply(TIER_3_MULTIPLIER), currency.name()),
                TIER_1_MAX,
                TIER_2_MAX,
                TIER_3_MAX);
    }

    public record PurchaseQuoteResult(
            Long companyId,
            String companyName,
            Integer credits,
            BigDecimal basePrice,
            BigDecimal totalAmount,
            BillingCurrency currency,
            String currencySymbol,
            BigDecimal pricePerCredit,
            String appliedTier,
            boolean qualifiesForContactSales) {
    }

    public PurchaseQuoteResult getQuote(Long companyId, Integer credits) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));

        BillingCurrency currency = resolveBillingCurrency(company);
        CreditPlan plan = resolveCreditPlan(company);
        int historicalPurchased = company.getHistoricalCreditsPurchased();
        String currencySymbol = exchangeRateService.getCurrencySymbol(currency.name());

        TierPricing tier = computeTierPricing(historicalPurchased, plan.getBasePriceUsd(), currency, currencySymbol);

        if (tier.qualifiesForContactSales()) {
            return new PurchaseQuoteResult(
                    companyId,
                    company.getName(),
                    credits,
                    tier.pricePerCredit().multiply(BigDecimal.valueOf(credits)),
                    tier.pricePerCredit().multiply(BigDecimal.valueOf(credits)),
                    currency,
                    currencySymbol,
                    tier.pricePerCredit(),
                    tier.appliedTier(),
                    true);
        }

        BigDecimal basePrice = tier.pricePerCredit().multiply(BigDecimal.valueOf(credits));

        return new PurchaseQuoteResult(
                companyId,
                company.getName(),
                credits,
                basePrice,
                basePrice,
                currency,
                currencySymbol,
                tier.pricePerCredit(),
                tier.appliedTier(),
                false);
    }

    public record InitiatePurchaseResult(
            String txRef,
            String paymentLink,
            Integer credits,
            BigDecimal totalAmount,
            BillingCurrency currency,
            String currencySymbol,
            Long purchaseId) {
    }

    public InitiatePurchaseResult initiatePurchase(Long userId, Long companyId, Integer credits) {
        return initiatePurchase(userId, companyId, credits, "ADMIN_CREDITS");
    }

    public InitiatePurchaseResult initiatePurchase(Long userId, Long companyId, Integer credits,
            String callbackType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));

        BillingCurrency currency = resolveBillingCurrency(company);
        CreditPlan plan = resolveCreditPlan(company);
        int historicalPurchased = company.getHistoricalCreditsPurchased();
        String currencySymbol = exchangeRateService.getCurrencySymbol(currency.name());

        TierPricing tier = computeTierPricing(historicalPurchased, plan.getBasePriceUsd(), currency, currencySymbol);

        if (tier.qualifiesForContactSales()) {
            throw new IllegalStateException("Companies with 500+ historical credits must contact sales for custom pricing.");
        }

        BigDecimal pricePerCredit = tier.pricePerCredit();
        BigDecimal totalAmount = pricePerCredit.multiply(BigDecimal.valueOf(credits));

        String txRef = flutterwaveService.generateTransactionReference();

        CreditPurchase purchase = new CreditPurchase();
        purchase.setTxRef(txRef);
        purchase.setUser(user);
        purchase.setCompanyId(companyId);
        purchase.setCreditsPurchased(credits);
        purchase.setCurrency(currency);
        purchase.setCurrencySymbol(currencySymbol);
        purchase.setPricePerCredit(pricePerCredit);
        purchase.setAmount(totalAmount);
        purchase.setStatus("pending");
        purchaseRepository.save(purchase);

        String redirectUrl = callbackRegistry.getBackendCallbackUrl(callbackType);

        FlutterwavePaymentRequest paymentRequest = new FlutterwavePaymentRequest(
                totalAmount,
                currency.name(),
                user.getEmail(),
                user.getName() != null ? user.getName() : user.getEmail(),
                "TMAG Company Credit Purchase - " + company.getName() + " - " + credits + " credits",
                txRef,
                user.getPhone(),
                redirectUrl,
                credits,
                null,
                userId.toString(),
                companyId.toString());

        FlutterwavePaymentResponse paymentResponse = flutterwaveService.initiatePayment(paymentRequest);

        if (paymentResponse.success() && paymentResponse.paymentLink() != null) {
            logger.info(
                    "Flutterwave payment initiated for company purchase: txRef={}, companyId={}, credits={}, amount={}, callbackType={}",
                    txRef, companyId, credits, totalAmount, callbackType);

            return new InitiatePurchaseResult(
                    txRef,
                    paymentResponse.paymentLink(),
                    credits,
                    totalAmount,
                    currency,
                    currencySymbol,
                    purchase.getId());
        } else {
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

        FlutterwavePaymentResponse verification;
        if (transactionId != null && !transactionId.isBlank()) {
            verification = flutterwaveService.verifyTransaction(transactionId);
        } else {
            verification = flutterwaveService.verifyTransactionByReference(txRef);
        }

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
            return CreditPurchaseResponse.from(purchase);
        }
    }

    private CreditPurchaseResponse completePurchase(CreditPurchase purchase, FlutterwavePaymentResponse verification) {
        purchase.setStatus("completed");
        purchase.setPaidAt(LocalDateTime.now());
        purchase.setFlutterwaveStatus(verification.status());
        purchase.setFlwRef(verification.flwRef());
        purchase.setAmountPaid(verification.amount() != null ? verification.amount() : purchase.getAmount());
        purchaseRepository.save(purchase);

        if (creditRepository.existsByTypeAndReference("purchase", purchase.getTxRef())) {
            logger.info("Credit entry already exists for txRef={}, skipping duplicate: txRef={}", purchase.getTxRef(),
                    purchase.getTxRef());
            return CreditPurchaseResponse.from(purchase);
        }

        if (purchase.getCompanyId() != null) {
            Company company = companyRepository.findById(purchase.getCompanyId())
                    .orElseThrow(() -> new NoSuchElementException("Company not found"));

            int currentTotal = company.getTotalCredits() != null ? company.getTotalCredits() : 0;
            int balanceAfter = currentTotal + purchase.getCreditsPurchased();
            company.setTotalCredits(balanceAfter);

            int currentPurchased = company.getTotalCreditsPurchased() != null ? company.getTotalCreditsPurchased() : 0;
            company.setTotalCreditsPurchased(currentPurchased + purchase.getCreditsPurchased());

            companyRepository.save(company);
            evictCompanyPricingCache(purchase.getCompanyId());

            Credit creditEntry = new Credit();
            creditEntry.setCompany(company);
            creditEntry.setAmount(purchase.getCreditsPurchased());
            creditEntry.setType("purchase");
            creditEntry.setReference(purchase.getTxRef());
            creditEntry.setBalanceAfter(balanceAfter);
            creditRepository.save(creditEntry);

            Invoice invoice = new Invoice();
            invoice.setAmount(purchase.getAmountPaid() != null ? purchase.getAmountPaid() : purchase.getAmount());
            invoice.setCurrency(purchase.getCurrency().name());
            invoice.setStatus("paid");
            invoice.setDescription(
                    "Credit Purchase - " + purchase.getCreditsPurchased() + " credits for " + company.getName());
            invoice.setIssuedAt(LocalDateTime.now());
            invoice.setPaidAt(LocalDateTime.now());
            invoice.setPaymentMethod("Flutterwave");
            invoice.setCompany(company);
            invoiceRepository.save(invoice);

            logger.info("Company credit purchase completed: txRef={}, companyId={}, credits={}, invoiceId={}",
                    purchase.getTxRef(), purchase.getCompanyId(), purchase.getCreditsPurchased(), invoice.getId());
        } else {
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

            Invoice invoice = new Invoice();
            invoice.setAmount(purchase.getAmountPaid() != null ? purchase.getAmountPaid() : purchase.getAmount());
            invoice.setCurrency(purchase.getCurrency().name());
            invoice.setStatus("paid");
            invoice.setDescription("Credit Purchase - " + purchase.getCreditsPurchased() + " credits");
            invoice.setIssuedAt(LocalDateTime.now());
            invoice.setPaidAt(LocalDateTime.now());
            invoice.setPaymentMethod("Flutterwave");
            invoice.setUser(user);
            invoiceRepository.save(invoice);

            logger.info("User credit purchase completed: txRef={}, credits={}, invoiceId={}",
                    purchase.getTxRef(), purchase.getCreditsPurchased(), invoice.getId());
        }

        return CreditPurchaseResponse.from(purchase);
    }

    public List<CreditPurchaseResponse> getPurchaseHistory(Long companyId) {
        if (companyId != null) {
            return purchaseRepository.findByCompanyIdOrderByCreatedAtDesc(companyId)
                    .stream()
                    .map(CreditPurchaseResponse::from)
                    .toList();
        }
        return purchaseRepository.findByStatus("completed")
                .stream()
                .filter(p -> p.getCompanyId() != null)
                .map(CreditPurchaseResponse::from)
                .toList();
    }

    public CreditPurchaseResponse getPurchaseByTxRef(String txRef) {
        CreditPurchase purchase = purchaseRepository.findByTxRef(txRef)
                .orElseThrow(() -> new NoSuchElementException("Purchase not found"));
        return CreditPurchaseResponse.from(purchase);
    }

    private record TierPricing(
            BigDecimal pricePerCredit,
            String appliedTier,
            boolean qualifiesForContactSales
    ) {}

    private TierPricing computeTierPricing(int historicalCredits, BigDecimal basePriceUsd,
            BillingCurrency currency, String currencySymbol) {
        BigDecimal priceInCurrency;
        String tier;
        boolean contactSales;

        if (historicalCredits >= 500) {
            tier = "TIER_3";
            contactSales = true;
        } else if (historicalCredits >= 100) {
            tier = "TIER_3";
            contactSales = false;
        } else if (historicalCredits >= 50) {
            tier = "TIER_2";
            contactSales = false;
        } else {
            tier = "TIER_1";
            contactSales = false;
        }

        BigDecimal multiplier = switch (tier) {
            case "TIER_3" -> TIER_3_MULTIPLIER;
            case "TIER_2" -> TIER_2_MULTIPLIER;
            default -> TIER_1_MULTIPLIER;
        };

        BigDecimal tierBaseUsd = basePriceUsd.multiply(multiplier);
        priceInCurrency = exchangeRateService.convertFromUsd(tierBaseUsd, currency.name());

        return new TierPricing(priceInCurrency, tier, contactSales);
    }

    private CreditPlan resolveCreditPlan(Company company) {
        if (company.getCreditPlan() != null) {
            return company.getCreditPlan();
        }
        return userCreditPlanRepository.findByIsDefaultTrue()
                .orElseGet(() -> userCreditPlanRepository.findAll().stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No credit plan configured")));
    }

    private void evictCompanyPricingCache(Long companyId) {
        if (companyId == null) {
            return;
        }
        var cache = cacheManager.getCache(CacheNames.COMPANY_ADMIN_CREDITS_PRICING);
        if (cache != null) {
            cache.evict(companyId);
        }
    }

    private BillingCurrency resolveBillingCurrency(Company company) {
        return settingRepository.findByCompanyIdAndKeyAndDeletedAtIsNull(company.getId(), "pref_currency")
                .map(setting -> {
                    try {
                        return BillingCurrency.valueOf(setting.getValue().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return company.getBillingCurrency() != null
                                ? company.getBillingCurrency()
                                : BillingCurrency.NGN;
                    }
                })
                .orElseGet(() -> company.getBillingCurrency() != null
                        ? company.getBillingCurrency()
                        : BillingCurrency.NGN);
    }
}