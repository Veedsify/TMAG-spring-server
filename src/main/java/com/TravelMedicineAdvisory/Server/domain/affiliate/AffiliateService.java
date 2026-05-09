package com.TravelMedicineAdvisory.Server.domain.affiliate;

import com.TravelMedicineAdvisory.Server.core.email.EmailService;
import com.TravelMedicineAdvisory.Server.core.email.EmailTemplates;
import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.domain.creditpurchase.CreditPurchase;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlan;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.role.Role;
import com.TravelMedicineAdvisory.Server.domain.role.RoleRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AffiliateService {

    private static final BigDecimal DEFAULT_COMMISSION_RATE = BigDecimal.valueOf(10);
    private static final BigDecimal DEFAULT_DISCOUNT_RATE = BigDecimal.valueOf(2);
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final int COOKIE_DAYS = 90;
    private static final String CREDIT_PURCHASE_REFERENCE = "credit_purchase";

    private static final BigDecimal MIN_PAYOUT_USD = BigDecimal.valueOf(200);
    private static final BigDecimal MIN_PAYOUT_NGN = BigDecimal.valueOf(200_000);

    private final AffiliateProfileRepository affiliateProfileRepository;
    private final ReferralLinkRepository referralLinkRepository;
    private final AffiliateReferralRepository affiliateReferralRepository;
    private final AffiliateCommissionRepository affiliateCommissionRepository;
    private final AffiliatePayoutRepository affiliatePayoutRepository;
    private final AffiliateClickRepository affiliateClickRepository;
    private final UserRepository userRepository;
    private final CreditPlanRepository creditPlanRepository;
    private final AffiliateApplicationRepository affiliateApplicationRepository;
    private final EmailService emailService;
    private final EmailTemplates emailTemplates;
    private final QueueService queueService;
    private final RoleRepository roleRepository;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public AffiliateService(
            AffiliateProfileRepository affiliateProfileRepository,
            ReferralLinkRepository referralLinkRepository,
            AffiliateReferralRepository affiliateReferralRepository,
            AffiliateCommissionRepository affiliateCommissionRepository,
            AffiliatePayoutRepository affiliatePayoutRepository,
            AffiliateClickRepository affiliateClickRepository,
            UserRepository userRepository,
            CreditPlanRepository creditPlanRepository,
            AffiliateApplicationRepository affiliateApplicationRepository,
            EmailService emailService,
            EmailTemplates emailTemplates,
            QueueService queueService,
            RoleRepository roleRepository) {
        this.affiliateProfileRepository = affiliateProfileRepository;
        this.referralLinkRepository = referralLinkRepository;
        this.affiliateReferralRepository = affiliateReferralRepository;
        this.affiliateCommissionRepository = affiliateCommissionRepository;
        this.affiliatePayoutRepository = affiliatePayoutRepository;
        this.affiliateClickRepository = affiliateClickRepository;
        this.userRepository = userRepository;
        this.creditPlanRepository = creditPlanRepository;
        this.affiliateApplicationRepository = affiliateApplicationRepository;
        this.emailService = emailService;
        this.emailTemplates = emailTemplates;
        this.queueService = queueService;
        this.roleRepository = roleRepository;
    }

    public record AffiliatePurchaseDiscount(BigDecimal rate, String referralCode) {}

    @Transactional(readOnly = true)
    public Optional<AffiliateDiscountResponse> getDiscount(String referralCode) {
        return resolveReferralTarget(referralCode)
                .filter(target -> isActive(target.affiliateProfile()))
                .map(target -> new AffiliateDiscountResponse(
                        true,
                        target.referralLink() != null ? target.referralLink().getShortCode() : null,
                        target.affiliateProfile().getReferralCode(),
                        safeRate(target.affiliateProfile().getDiscountRate(), DEFAULT_DISCOUNT_RATE)
                ));
    }

    public AffiliateProfileResponse getProfile(Long userId) {
        return AffiliateProfileResponse.from(getOrCreateAffiliateProfile(userId));
    }

    public List<ReferralLinkResponse> getLinks(Long userId) {
        AffiliateProfile profile = getOrCreateAffiliateProfile(userId);
        return referralLinkRepository.findByAffiliateProfileIdAndDeletedAtIsNullOrderByCreatedAtDesc(profile.getId())
                .stream()
                .map(ReferralLinkResponse::from)
                .toList();
    }

    public ReferralLinkResponse createReferralLink(Long userId, CreateReferralLinkRequest request) {
        AffiliateProfile profile = getOrCreateAffiliateProfile(userId);
        if (!isActive(profile)) {
            throw new IllegalStateException("Affiliate account is not active");
        }

        CreditPlan creditPlan = resolveCreditPlan(request);
        String destinationUrl = resolveDestinationUrl(request, creditPlan);
        String campaign = request.campaign() != null && !request.campaign().isBlank()
                ? request.campaign().trim()
                : creditPlan != null ? creditPlan.getDisplayName() : "Referral campaign";

        ReferralLink link = new ReferralLink();
        link.setAffiliateProfile(profile);
        link.setCreditPlan(creditPlan);
        link.setCampaign(campaign);
        link.setDestinationUrl(destinationUrl);
        link.setShortCode(generateShortCode());
        link.setIsActive(true);

        return ReferralLinkResponse.from(referralLinkRepository.save(link));
    }

    public List<CommissionRecordResponse> getCommissions(Long userId) {
        return getCommissions(userId, null, null);
    }

    public List<CommissionRecordResponse> getCommissions(Long userId, String startDate, String endDate) {
        AffiliateProfile profile = getOrCreateAffiliateProfile(userId);
        LocalDateTime from = parseStartDate(startDate);
        LocalDateTime to = parseEndDate(endDate);
        return affiliateCommissionRepository.findByAffiliateProfileIdAndDeletedAtIsNullOrderByCreatedAtDesc(profile.getId())
                .stream()
                .filter(c -> c.getCreatedAt() != null && isWithinRange(c.getCreatedAt(), from, to))
                .map(CommissionRecordResponse::from)
                .toList();
    }

    public List<PayoutRecordResponse> getPayouts(Long userId) {
        AffiliateProfile profile = getOrCreateAffiliateProfile(userId);
        return affiliatePayoutRepository.findByAffiliateProfileIdAndDeletedAtIsNullOrderByRequestedAtDesc(profile.getId())
                .stream()
                .map(PayoutRecordResponse::from)
                .toList();
    }

    public PayoutRecordResponse requestPayout(Long userId, PayoutRequest request) {
        AffiliateProfile profile = getOrCreateAffiliateProfile(userId);
        BigDecimal amount = money(request.amount());
        String payoutCurrency = request.currency() != null ? request.currency().toUpperCase() : "USD";

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payout amount must be greater than zero");
        }

        // ── Enforce minimum payout ────────────────────────────────────────
        BigDecimal minAmount = "NGN".equals(payoutCurrency) ? MIN_PAYOUT_NGN : MIN_PAYOUT_USD;
        if (amount.compareTo(minAmount) < 0) {
            String sym = "NGN".equals(payoutCurrency) ? "\u20a6" : "$";
            throw new IllegalArgumentException(
                    "Minimum payout is " + sym + minAmount.toPlainString()
                            + " for " + payoutCurrency + " payouts");
        }

        BigDecimal available = "NGN".equals(payoutCurrency)
                ? nullToZero(profile.getPendingCommissionNgn())
                : nullToZero(profile.getPendingCommission());
        if (amount.compareTo(available) > 0) {
            throw new IllegalArgumentException("Payout amount exceeds available commission");
        }

        AffiliatePayout payout = new AffiliatePayout();
        payout.setAffiliateProfile(profile);
        payout.setAmount(amount);
        payout.setCurrency(payoutCurrency);
        payout.setPaymentMethod(request.paymentMethod().trim());
        payout.setPaymentDetails(request.paymentDetails().trim());
        payout.setStatus("pending");
        payout.setRequestedAt(LocalDateTime.now());

        if ("NGN".equals(payoutCurrency)) {
            profile.setPendingCommissionNgn(nullToZero(profile.getPendingCommissionNgn()).subtract(amount));
        } else {
            profile.setPendingCommission(nullToZero(profile.getPendingCommission()).subtract(amount));
        }
        affiliateProfileRepository.save(profile);

        AffiliatePayout saved = affiliatePayoutRepository.save(payout);

        // ── Notify affiliate: payout request received ─────────────────────────
        try {
            User affiliateUser = profile.getUser();
            if (affiliateUser != null && affiliateUser.getEmail() != null) {
                String firstName = affiliateUser.getFirstName() != null ? affiliateUser.getFirstName() : "there";
                emailService.sendAffiliatePayoutRequested(
                        affiliateUser.getEmail(),
                        firstName,
                        amount.toPlainString(),
                        payoutCurrency,
                        payout.getPaymentMethod(),
                        payout.getPaymentDetails()
                );
            }
        } catch (Exception e) {
            // Log but don't fail the payout request
        }

        // ── Notify every super-admin: new payout request ──────────────────────
        try {
            User affiliateUser = profile.getUser();
            String affiliateName = affiliateUser != null ? affiliateUser.getFullName() : "Unknown Affiliate";
            String affiliateEmail = affiliateUser != null && affiliateUser.getEmail() != null
                    ? affiliateUser.getEmail() : "unknown";

            List<User> superAdmins = userRepository.findByRoleName("SuperAdmin");
            for (User admin : superAdmins) {
                emailService.sendAffiliatePayoutRequestedAdmin(
                        admin.getEmail(),
                        affiliateName,
                        affiliateEmail,
                        amount.toPlainString(),
                        payoutCurrency,
                        payout.getPaymentMethod(),
                        payout.getPaymentDetails()
                );
            }
        } catch (Exception e) {
            // Log but don't fail the payout request
        }

        return PayoutRecordResponse.from(saved);
    }

    public AffiliateStatsResponse getStats(Long userId) {
        return getStats(userId, null, null);
    }

    public AffiliateStatsResponse getStats(Long userId, String startDate, String endDate) {
        AffiliateProfile profile = getOrCreateAffiliateProfile(userId);
        List<ReferralLink> links = referralLinkRepository.findByAffiliateProfileIdAndDeletedAtIsNullOrderByCreatedAtDesc(profile.getId());
        List<AffiliateCommission> commissions = affiliateCommissionRepository.findByAffiliateProfileIdAndDeletedAtIsNullOrderByCreatedAtDesc(profile.getId());
        LocalDateTime from = parseStartDate(startDate);
        LocalDateTime to = parseEndDate(endDate);
        boolean isPeriodFiltered = from != null || to != null;

        // Filter commissions by date range
        List<AffiliateCommission> filteredCommissions = isPeriodFiltered
                ? commissions.stream()
                        .filter(c -> c.getCreatedAt() != null && isWithinRange(c.getCreatedAt(), from, to))
                        .toList()
                : commissions;

        // Compute clicks for the period (overall or date-filtered)
        long periodClicks;
        if (isPeriodFiltered) {
            periodClicks = affiliateClickRepository
                    .countByAffiliateProfileIdAndCreatedAtBetweenAndDeletedAtIsNull(
                            profile.getId(), from != null ? from : LocalDateTime.MIN,
                            to != null ? to : LocalDateTime.MAX);
        } else {
            periodClicks = profile.getTotalClicks() != null ? profile.getTotalClicks() : 0;
        }

        // Compute conversions for the period
        long periodConversions;
        if (isPeriodFiltered) {
            periodConversions = filteredCommissions.size();
        } else {
            periodConversions = profile.getTotalConversions() != null ? profile.getTotalConversions() : 0;
        }

        // Commission totals for the period
        BigDecimal periodTotalCommission = filteredCommissions.stream()
                .filter(c -> !"NGN".equals(c.getCurrency()))
                .map(c -> nullToZero(c.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal periodTotalCommissionNgn = filteredCommissions.stream()
                .filter(c -> "NGN".equals(c.getCurrency()))
                .map(c -> nullToZero(c.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long clicks = isPeriodFiltered ? periodClicks : (profile.getTotalClicks() != null ? profile.getTotalClicks() : 0);
        long conversions = isPeriodFiltered ? periodConversions : (profile.getTotalConversions() != null ? profile.getTotalConversions() : 0);
        BigDecimal conversionRate = clicks > 0
                ? BigDecimal.valueOf(conversions).multiply(ONE_HUNDRED).divide(BigDecimal.valueOf(clicks), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        List<AffiliateStatsResponse.TopCampaignResponse> topCampaigns = links.stream()
                .sorted((a, b) -> {
                    int byCommission = nullToZero(b.getCommissionEarned()).compareTo(nullToZero(a.getCommissionEarned()));
                    if (byCommission != 0) {
                        return byCommission;
                    }
                    return Integer.compare(b.getClicks() != null ? b.getClicks() : 0, a.getClicks() != null ? a.getClicks() : 0);
                })
                .limit(5)
                .map(link -> new AffiliateStatsResponse.TopCampaignResponse(
                        link.getCampaign(),
                        link.getClicks(),
                        link.getConversions(),
                        nullToZero(link.getCommissionEarned())
                ))
                .toList();

        List<CommissionRecordResponse> recentCommissions = filteredCommissions.stream()
                .limit(5)
                .map(CommissionRecordResponse::from)
                .toList();

        // For period-filtered stats, use the profile's overall totals for pending/paid
        // (those are not per-period concepts, they represent the overall balance)
        return new AffiliateStatsResponse(
                (int) clicks,
                (int) conversions,
                conversionRate,
                isPeriodFiltered ? periodTotalCommission : nullToZero(profile.getTotalCommissionEarned()),
                nullToZero(profile.getPendingCommission()),
                nullToZero(profile.getTotalPaidOut()),
                isPeriodFiltered ? periodTotalCommissionNgn : nullToZero(profile.getTotalCommissionEarnedNgn()),
                nullToZero(profile.getPendingCommissionNgn()),
                nullToZero(profile.getTotalPaidOutNgn()),
                referralLinkRepository.countByAffiliateProfileIdAndIsActiveTrueAndDeletedAtIsNull(profile.getId()),
                topCampaigns,
                recentCommissions,
                buildClicksChart(profile.getId(), commissions)
        );
    }

    public AffiliateTrackingResponse trackClick(String shortCode, String ipAddress, String userAgent) {
        ReferralLink link = referralLinkRepository.findByShortCodeAndIsActiveTrueAndDeletedAtIsNull(normalizeCode(shortCode))
                .orElseThrow(() -> new NoSuchElementException("Referral link not found"));
        AffiliateProfile profile = link.getAffiliateProfile();
        if (!isActive(profile)) {
            throw new NoSuchElementException("Referral link not found");
        }

        link.setClicks((link.getClicks() != null ? link.getClicks() : 0) + 1);
        profile.setTotalClicks((profile.getTotalClicks() != null ? profile.getTotalClicks() : 0) + 1);
        referralLinkRepository.save(link);
        affiliateProfileRepository.save(profile);

        AffiliateClick click = new AffiliateClick();
        click.setAffiliateProfile(profile);
        click.setReferralLink(link);
        click.setIpAddress(truncate(ipAddress, 80));
        click.setUserAgent(userAgent);
        affiliateClickRepository.save(click);

        return new AffiliateTrackingResponse(
                link.getShortCode(),
                profile.getReferralCode(),
                link.getDestinationUrl(),
                safeRate(profile.getDiscountRate(), DEFAULT_DISCOUNT_RATE),
                COOKIE_DAYS
        );
    }

    public void registerReferralForUser(Long userId, String referralCode) {
        if (userId == null || referralCode == null || referralCode.isBlank()) {
            return;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }
        createReferralIfMissing(user, referralCode);
    }

    public Optional<AffiliatePurchaseDiscount> resolveDiscountForUser(User user, String referralCode) {
        if (user == null) {
            return Optional.empty();
        }

        Optional<AffiliateReferral> existingReferral = affiliateReferralRepository.findByReferredUserIdAndDeletedAtIsNull(user.getId());
        if (existingReferral.isPresent() && isActive(existingReferral.get().getAffiliateProfile())) {
            AffiliateProfile profile = existingReferral.get().getAffiliateProfile();
            return Optional.of(new AffiliatePurchaseDiscount(
                    safeRate(profile.getDiscountRate(), DEFAULT_DISCOUNT_RATE),
                    existingReferral.get().getReferralCode()
            ));
        }

        return createReferralIfMissing(user, referralCode)
                .map(referral -> new AffiliatePurchaseDiscount(
                        safeRate(referral.getAffiliateProfile().getDiscountRate(), DEFAULT_DISCOUNT_RATE),
                        referral.getReferralCode()
                ));
    }

    public AffiliateApplicationResponse submitApplication(AffiliateApplicationRequest request) {
        if (affiliateApplicationRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new IllegalStateException("An application with this email already exists");
        }
        AffiliateApplication application = new AffiliateApplication();
        application.setFullName(request.fullName());
        application.setCompanyName(request.companyName());
        application.setEmail(request.email().trim().toLowerCase());
        application.setPhone(request.phone());
        application.setWebsiteUrl(request.websiteUrl());
        application.setSocialMediaLinks(request.socialMediaLinks());
        application.setEstimatedMonthlyReach(request.estimatedMonthlyReach());
        application.setPromoDescription(request.promoDescription());
        application.setAgreedToTerms(request.agreedToTerms());
        application.setStatus("pending");
        AffiliateApplicationResponse response = AffiliateApplicationResponse.from(affiliateApplicationRepository.save(application));

        // Send confirmation email to applicant
        try {
            String[] nameParts = request.fullName().trim().split("\\s+", 2);
            String firstName = nameParts[0];
            emailService.sendAffiliateApplicationConfirmation(request.email(), firstName);
        } catch (Exception e) {
            // Log but don't fail the submission
        }

        // Notify super-admins of new application
        try {
            List<User> superAdmins = userRepository.findByRoleName("SuperAdmin");
            String safeName = request.fullName() != null ? request.fullName().replace("<", "&lt;").replace(">", "&gt;") : "";
            String safeEmail = request.email() != null ? request.email().replace("<", "&lt;").replace(">", "&gt;") : "";
            String html = "<p>A new affiliate application has been submitted by <strong>" + safeName + "</strong> (" + safeEmail + ").</p>"
                    + "<p>Log in to the admin dashboard to review.</p>";
            for (User admin : superAdmins) {
                queueService.dispatch(JobType.EMAIL_GENERIC, Map.of(
                        "to", admin.getEmail(),
                        "subject", "New Affiliate Application: " + request.fullName(),
                        "html", html
                ));
            }
        } catch (Exception e) {
            // Log but don't fail
        }

        return response;
    }

    public String generateReferralCodeForUser(User user) {
        return generateReferralCode(user);
    }

    public void recordCommissionForCompletedPurchase(CreditPurchase purchase) {
        if (purchase == null || purchase.getId() == null || purchase.getUser() == null) {
            return;
        }
        if (!"completed".equalsIgnoreCase(purchase.getStatus())) {
            return;
        }
        if (affiliateCommissionRepository.existsByReferenceTypeAndReferenceIdAndDeletedAtIsNull(CREDIT_PURCHASE_REFERENCE, purchase.getId())) {
            return;
        }

        Optional<AffiliateReferral> referralOpt = affiliateReferralRepository.findByReferredUserIdAndDeletedAtIsNull(purchase.getUser().getId());
        if (referralOpt.isEmpty()) {
            return;
        }

        AffiliateReferral referral = referralOpt.get();
        AffiliateProfile profile = referral.getAffiliateProfile();
        if (!isActive(profile)) {
            return;
        }

        BigDecimal baseAmount = money(purchase.getAmountPaid() != null ? purchase.getAmountPaid() : purchase.getAmount());
        if (baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        String purchaseCurrency = purchase.getCurrency() != null ? purchase.getCurrency().name() : "USD";

        BigDecimal rate = safeRate(profile.getCommissionRate(), DEFAULT_COMMISSION_RATE);
        BigDecimal commissionAmount = money(baseAmount.multiply(rate).divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP));

        AffiliateCommission commission = new AffiliateCommission();
        commission.setAffiliateProfile(profile);
        commission.setReferralLink(referral.getReferralLink());
        commission.setReferredUser(purchase.getUser());
        commission.setAmount(commissionAmount);
        commission.setBaseAmount(baseAmount);
        commission.setRate(rate);
        commission.setStatus("approved");
        commission.setCurrency(purchaseCurrency);
        commission.setCustomerEmail(purchase.getUser().getEmail());
        commission.setReferenceType(CREDIT_PURCHASE_REFERENCE);
        commission.setReferenceId(purchase.getId());
        affiliateCommissionRepository.save(commission);

        // Track currency-specific totals
        if ("NGN".equals(purchaseCurrency)) {
            profile.setTotalCommissionEarnedNgn(nullToZero(profile.getTotalCommissionEarnedNgn()).add(commissionAmount));
            profile.setPendingCommissionNgn(nullToZero(profile.getPendingCommissionNgn()).add(commissionAmount));
        } else {
            profile.setTotalCommissionEarned(nullToZero(profile.getTotalCommissionEarned()).add(commissionAmount));
            profile.setPendingCommission(nullToZero(profile.getPendingCommission()).add(commissionAmount));
        }
        affiliateProfileRepository.save(profile);

        ReferralLink link = referral.getReferralLink();
        if (link != null) {
            link.setCommissionEarned(nullToZero(link.getCommissionEarned()).add(commissionAmount));
            referralLinkRepository.save(link);
        }

        // Send commission earned email to affiliate
        try {
            User affiliateUser = profile.getUser();
            if (affiliateUser != null && affiliateUser.getEmail() != null) {
                String affiliateName = affiliateUser.getFirstName() != null ? affiliateUser.getFirstName() : "there";
                String campaign = link != null ? link.getCampaign() : "General";
                emailService.sendAffiliateCommissionEarned(
                        affiliateUser.getEmail(),
                        affiliateName,
                        "$" + commissionAmount.toPlainString(),
                        purchase.getUser().getEmail(),
                        campaign
                );
            }
        } catch (Exception e) {
            // Log but don't fail
        }
    }

    private Optional<AffiliateReferral> createReferralIfMissing(User user, String referralCode) {
        if (user == null || referralCode == null || referralCode.isBlank()) {
            return Optional.empty();
        }
        Optional<AffiliateReferral> existingReferral = affiliateReferralRepository.findByReferredUserIdAndDeletedAtIsNull(user.getId());
        if (existingReferral.isPresent()) {
            return existingReferral;
        }

        Optional<ReferralTarget> targetOpt = resolveReferralTarget(referralCode)
                .filter(target -> isActive(target.affiliateProfile()));
        if (targetOpt.isEmpty()) {
            return Optional.empty();
        }

        ReferralTarget target = targetOpt.get();
        if (target.affiliateProfile().getUser() != null && user.getId().equals(target.affiliateProfile().getUser().getId())) {
            return Optional.empty();
        }

        AffiliateReferral referral = new AffiliateReferral();
        referral.setAffiliateProfile(target.affiliateProfile());
        referral.setReferralLink(target.referralLink());
        referral.setReferredUser(user);
        referral.setReferralCode(target.referralLink() != null ? target.referralLink().getShortCode() : target.affiliateProfile().getReferralCode());
        referral.setStatus("active");
        referral.setFirstClickAt(LocalDateTime.now());
        referral.setConvertedAt(LocalDateTime.now());
        AffiliateReferral savedReferral = affiliateReferralRepository.save(referral);

        AffiliateProfile profile = target.affiliateProfile();
        profile.setTotalConversions((profile.getTotalConversions() != null ? profile.getTotalConversions() : 0) + 1);
        affiliateProfileRepository.save(profile);

        ReferralLink link = target.referralLink();
        if (link != null) {
            link.setConversions((link.getConversions() != null ? link.getConversions() : 0) + 1);
            referralLinkRepository.save(link);
        }

        return Optional.of(savedReferral);
    }

    private AffiliateProfile getOrCreateAffiliateProfile(Long userId) {
        return affiliateProfileRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseGet(() -> createAffiliateProfile(userId));
    }

    private AffiliateProfile createAffiliateProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        AffiliateProfile profile = new AffiliateProfile();
        profile.setUser(user);
        profile.setReferralCode(generateReferralCode(user));
        profile.setCommissionRate(DEFAULT_COMMISSION_RATE);
        profile.setDiscountRate(DEFAULT_DISCOUNT_RATE);
        profile.setStatus("active");
        return affiliateProfileRepository.save(profile);
    }

    private CreditPlan resolveCreditPlan(CreateReferralLinkRequest request) {
        if (request.creditPlanId() != null) {
            return creditPlanRepository.findById(request.creditPlanId())
                    .orElseThrow(() -> new NoSuchElementException("Credit plan not found"));
        }
        if (request.creditPlanCode() != null && !request.creditPlanCode().isBlank()) {
            return creditPlanRepository.findByCode(request.creditPlanCode().trim().toUpperCase())
                    .orElseThrow(() -> new NoSuchElementException("Credit plan not found"));
        }
        return null;
    }

    private String resolveDestinationUrl(CreateReferralLinkRequest request, CreditPlan creditPlan) {
        if (request.destinationUrl() != null && !request.destinationUrl().isBlank()) {
            return request.destinationUrl().trim();
        }
        if (creditPlan != null) {
            return trimTrailingSlash(frontendUrl) + "/register?plan=" + creditPlan.getCode();
        }
        throw new IllegalArgumentException("Either destination_url or credit_plan_id is required");
    }

    private Optional<ReferralTarget> resolveReferralTarget(String referralCode) {
        String code = normalizeCode(referralCode);
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        Optional<ReferralLink> link = referralLinkRepository.findByShortCodeAndIsActiveTrueAndDeletedAtIsNull(code);
        if (link.isPresent()) {
            return Optional.of(new ReferralTarget(link.get().getAffiliateProfile(), link.get()));
        }
        return affiliateProfileRepository.findByReferralCodeAndDeletedAtIsNull(code)
                .map(profile -> new ReferralTarget(profile, null));
    }

    private List<AffiliateStatsResponse.ClicksChartPointResponse> buildClicksChart(Long affiliateId, List<AffiliateCommission> commissions) {
        LocalDate start = LocalDate.now().minusDays(13);
        LocalDateTime createdAfter = start.atStartOfDay();
        Map<LocalDate, ChartCounts> chart = new LinkedHashMap<>();
        for (int i = 0; i < 14; i++) {
            chart.put(start.plusDays(i), new ChartCounts());
        }

        affiliateClickRepository.findByAffiliateProfileIdAndCreatedAtAfterAndDeletedAtIsNull(affiliateId, createdAfter)
                .forEach(click -> {
                    if (click.getCreatedAt() != null) {
                        ChartCounts counts = chart.get(click.getCreatedAt().toLocalDate());
                        if (counts != null) {
                            counts.clicks++;
                        }
                    }
                });

        commissions.stream()
                .filter(commission -> commission.getCreatedAt() != null && !commission.getCreatedAt().isBefore(createdAfter))
                .forEach(commission -> {
                    ChartCounts counts = chart.get(commission.getCreatedAt().toLocalDate());
                    if (counts != null) {
                        counts.conversions++;
                    }
                });

        List<AffiliateStatsResponse.ClicksChartPointResponse> result = new ArrayList<>();
        chart.forEach((date, counts) -> result.add(new AffiliateStatsResponse.ClicksChartPointResponse(
                date.toString(), counts.clicks, counts.conversions
        )));
        return result;
    }

    private String generateReferralCode(User user) {
        String raw = user.getFirstName() != null && !user.getFirstName().isBlank()
                ? user.getFirstName()
                : user.getEmail() != null ? user.getEmail().split("@")[0] : "TMAG";
        String prefix = raw.toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (prefix.length() > 8) {
            prefix = prefix.substring(0, 8);
        }
        if (prefix.isBlank()) {
            prefix = "TMAG";
        }
        String code;
        do {
            code = prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        } while (affiliateProfileRepository.existsByReferralCode(code));
        return code;
    }

    private String generateShortCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        } while (referralLinkRepository.existsByShortCode(code));
        return code;
    }

    private boolean isActive(AffiliateProfile profile) {
        return profile != null && "active".equalsIgnoreCase(profile.getStatus());
    }

    private BigDecimal safeRate(BigDecimal value, BigDecimal fallback) {
        return value != null ? value : fallback;
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal money(BigDecimal amount) {
        return (amount != null ? amount : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeCode(String code) {
        return code != null ? code.trim().toUpperCase() : null;
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:3000";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private LocalDateTime parseStartDate(String startDate) {
        if (startDate == null || startDate.isBlank()) return null;
        try {
            return LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private LocalDateTime parseEndDate(String endDate) {
        if (endDate == null || endDate.isBlank()) return null;
        try {
            return LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE).plusDays(1).atStartOfDay();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private boolean isWithinRange(LocalDateTime dateTime, LocalDateTime from, LocalDateTime to) {
        if (from != null && dateTime.isBefore(from)) return false;
        if (to != null && dateTime.isAfter(to)) return false;
        return true;
    }

    private record ReferralTarget(AffiliateProfile affiliateProfile, ReferralLink referralLink) {}

    private static final class ChartCounts {
        private int clicks;
        private int conversions;
    }
}
