package com.TravelMedicineAdvisory.Server.domain.companyadmin.credits;

import com.TravelMedicineAdvisory.Server.core.payment.FlutterwavePaymentRequest;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwavePaymentResponse;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwaveService;
import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.companysetting.CompanySetting;
import com.TravelMedicineAdvisory.Server.domain.companysetting.CompanySettingRepository;
import com.TravelMedicineAdvisory.Server.domain.credit.Credit;
import com.TravelMedicineAdvisory.Server.domain.credit.CreditRepository;
import com.TravelMedicineAdvisory.Server.domain.creditpricing.CreditPricing;
import com.TravelMedicineAdvisory.Server.domain.creditpricing.CreditPricingService;
import com.TravelMedicineAdvisory.Server.domain.creditpurchase.CreditPurchase;
import com.TravelMedicineAdvisory.Server.domain.creditpurchase.CreditPurchaseRepository;
import com.TravelMedicineAdvisory.Server.domain.creditpurchase.CreditPurchaseResponse;
import com.TravelMedicineAdvisory.Server.domain.invoice.Invoice;
import com.TravelMedicineAdvisory.Server.domain.invoice.InvoiceRepository;
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
import java.util.NoSuchElementException;

@Service
@Transactional
public class CompanyAdminCreditPurchaseService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyAdminCreditPurchaseService.class);

    private final CompanyRepository companyRepository;
    private final CompanySettingRepository settingRepository;
    private final CreditRepository creditRepository;
    private final UserRepository userRepository;
    private final CreditPricingService pricingService;
    private final CreditPurchaseRepository purchaseRepository;
    private final FlutterwaveService flutterwaveService;
    private final InvoiceRepository invoiceRepository;

    @Value("${app.payment.flutterwave.admin-callback-url:${app.payment.flutterwave.callback-url:http://localhost:3002/admin/credits/callback}}")
    private String callbackUrl;

    @Value("${app.payment.flutterwave.hr-callback-url:${app.payment.flutterwave.callback-url:http://localhost:3000/hr/billing/callback}}")
    private String hrCallbackUrl;

    public CompanyAdminCreditPurchaseService(
            CompanyRepository companyRepository,
            CompanySettingRepository settingRepository,
            CreditRepository creditRepository,
            UserRepository userRepository,
            CreditPricingService pricingService,
            CreditPurchaseRepository purchaseRepository,
            FlutterwaveService flutterwaveService,
            InvoiceRepository invoiceRepository) {
        this.companyRepository = companyRepository;
        this.settingRepository = settingRepository;
        this.creditRepository = creditRepository;
        this.userRepository = userRepository;
        this.pricingService = pricingService;
        this.purchaseRepository = purchaseRepository;
        this.flutterwaveService = flutterwaveService;
        this.invoiceRepository = invoiceRepository;
    }

    public record CompanyPricingResult(
            Long companyId,
            String companyName,
            BillingCurrency currency,
            String currencySymbol,
            BigDecimal pricePerCredit,
            Integer minCredits,
            Integer maxCredits,
            BigDecimal discountTier1Threshold,
            BigDecimal discountTier1Amount,
            BigDecimal discountTier2Threshold,
            BigDecimal discountTier2Amount,
            BigDecimal discountTier3Threshold,
            BigDecimal discountTier3Amount,
            Integer totalCredits,
            Integer usedCredits
    ) {}

    public CompanyPricingResult getCompanyPricing(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));

        BillingCurrency currency = resolveBillingCurrency(company);

        CreditPricing pricing = pricingService.getPricingEntity(currency);

        return new CompanyPricingResult(
                company.getId(),
                company.getName(),
                currency,
                pricing.getCurrencySymbol(),
                pricing.getPricePerCredit(),
                pricing.getMinCredits(),
                pricing.getMaxCredits(),
                pricing.getDiscountTier1Threshold(),
                pricing.getDiscountTier1Amount(),
                pricing.getDiscountTier2Threshold(),
                pricing.getDiscountTier2Amount(),
                pricing.getDiscountTier3Threshold(),
                pricing.getDiscountTier3Amount(),
                company.getTotalCredits() != null ? company.getTotalCredits() : 0,
                company.getUsedCredits() != null ? company.getUsedCredits() : 0
        );
    }

    public record PurchaseQuoteResult(
            Long companyId,
            String companyName,
            Integer credits,
            BigDecimal basePrice,
            BigDecimal discountAmount,
            BigDecimal totalAmount,
            BillingCurrency currency,
            String currencySymbol,
            BigDecimal pricePerCredit,
            String appliedDiscountTier
    ) {}

    public PurchaseQuoteResult getQuote(Long companyId, Integer credits) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));

        BillingCurrency currency = resolveBillingCurrency(company);

        var calculation = pricingService.calculatePriceWithDiscount(currency, credits);

        return new PurchaseQuoteResult(
                companyId,
                company.getName(),
                credits,
                calculation.basePrice(),
                calculation.discountAmount(),
                calculation.totalPrice(),
                currency,
                calculation.currencySymbol(),
                pricingService.getPricingEntity(currency).getPricePerCredit(),
                calculation.appliedDiscountTier()
        );
    }

    public record InitiatePurchaseResult(
            String txRef,
            String paymentLink,
            Integer credits,
            BigDecimal totalAmount,
            BillingCurrency currency,
            String currencySymbol,
            Long purchaseId
    ) {}

    public InitiatePurchaseResult initiatePurchase(Long userId, Long companyId, Integer credits) {
        return initiatePurchase(userId, companyId, credits, false);
    }

    public InitiatePurchaseResult initiatePurchase(Long userId, Long companyId, Integer credits, boolean useHrCallback) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("Company not found"));

        BillingCurrency currency = resolveBillingCurrency(company);

        CreditPricing pricing = pricingService.getPricingEntity(currency);

        if (credits < pricing.getMinCredits() || credits > pricing.getMaxCredits()) {
            throw new IllegalArgumentException("Credits must be between " + pricing.getMinCredits() + " and " + pricing.getMaxCredits());
        }

        var priceCalculation = pricingService.calculatePriceWithDiscount(currency, credits);
        String txRef = flutterwaveService.generateTransactionReference();

        CreditPurchase purchase = new CreditPurchase();
        purchase.setTxRef(txRef);
        purchase.setUser(user);
        purchase.setCompanyId(companyId);
        purchase.setCreditsPurchased(credits);
        purchase.setCurrency(currency);
        purchase.setCurrencySymbol(pricing.getCurrencySymbol());
        purchase.setPricePerCredit(pricing.getPricePerCredit());
        purchase.setAmount(priceCalculation.totalPrice());
        purchase.setStatus("pending");
        purchaseRepository.save(purchase);

        String redirectUrl = (useHrCallback ? hrCallbackUrl : callbackUrl) + "?tx_ref=" + txRef;

        FlutterwavePaymentRequest paymentRequest = new FlutterwavePaymentRequest(
                priceCalculation.totalPrice(),
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
                companyId.toString()
        );

        FlutterwavePaymentResponse paymentResponse = flutterwaveService.initiatePayment(paymentRequest);

        if (paymentResponse.success() && paymentResponse.paymentLink() != null) {
            logger.info("Flutterwave payment initiated for company purchase: txRef={}, companyId={}, credits={}, amount={}, hr={}",
                    txRef, companyId, credits, priceCalculation.totalPrice(), useHrCallback);

            return new InitiatePurchaseResult(
                    txRef,
                    paymentResponse.paymentLink(),
                    credits,
                    priceCalculation.totalPrice(),
                    currency,
                    pricing.getCurrencySymbol(),
                    purchase.getId()
            );
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

        if (purchase.getCompanyId() != null) {
            Company company = companyRepository.findById(purchase.getCompanyId())
                    .orElseThrow(() -> new NoSuchElementException("Company not found"));

            int currentTotal = company.getTotalCredits() != null ? company.getTotalCredits() : 0;
            int balanceAfter = currentTotal + purchase.getCreditsPurchased();
            company.setTotalCredits(balanceAfter);
            companyRepository.save(company);

            Credit creditEntry = new Credit();
            creditEntry.setCompany(company);
            creditEntry.setUser(purchase.getUser());
            creditEntry.setAmount(purchase.getCreditsPurchased());
            creditEntry.setType("purchase");
            creditEntry.setReference(purchase.getTxRef());
            creditEntry.setBalanceAfter(balanceAfter);
            creditRepository.save(creditEntry);

            Invoice invoice = new Invoice();
            invoice.setAmount(purchase.getAmountPaid() != null ? purchase.getAmountPaid() : purchase.getAmount());
            invoice.setCurrency(purchase.getCurrency().name());
            invoice.setStatus("paid");
            invoice.setDescription("Credit Purchase - " + purchase.getCreditsPurchased() + " credits for " + company.getName());
            invoice.setIssuedAt(LocalDateTime.now());
            invoice.setPaidAt(LocalDateTime.now());
            invoice.setPaymentMethod("Flutterwave");
            invoice.setCompany(company);
            invoice.setUser(purchase.getUser());
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
