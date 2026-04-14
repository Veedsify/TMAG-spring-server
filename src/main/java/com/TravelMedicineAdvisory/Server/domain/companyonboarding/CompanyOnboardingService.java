package com.TravelMedicineAdvisory.Server.domain.companyonboarding;

import com.TravelMedicineAdvisory.Server.core.currency.ExchangeRateService;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwavePaymentRequest;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwavePaymentResponse;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwaveService;
import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import com.TravelMedicineAdvisory.Server.domain.company.BillingStatus;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.company.Tier;
import com.TravelMedicineAdvisory.Server.domain.companyonboarding.CompanyOnboardingSubmitRequest.TeamMemberRequest;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUser;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserRepository;
import com.TravelMedicineAdvisory.Server.domain.credit.Credit;
import com.TravelMedicineAdvisory.Server.domain.credit.CreditRepository;
import com.TravelMedicineAdvisory.Server.domain.creditpurchase.CreditPurchase;
import com.TravelMedicineAdvisory.Server.domain.creditpurchase.CreditPurchaseRepository;
import com.TravelMedicineAdvisory.Server.domain.employee.Employee;
import com.TravelMedicineAdvisory.Server.domain.employee.EmployeeRepository;
import com.TravelMedicineAdvisory.Server.domain.role.Role;
import com.TravelMedicineAdvisory.Server.domain.role.RoleRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlan;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanCode;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanRepository;
import com.TravelMedicineAdvisory.Server.core.utils.RandomNumberGenerator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@Transactional
public class CompanyOnboardingService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyOnboardingService.class);

    private final CompanyOnboardingRepository onboardingRepository;
    private final CompanyRepository companyRepository;
    private final CreditPlanRepository userCreditPlanRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyUserRepository companyUserRepository;
    private final CreditRepository creditRepository;
    private final CreditPurchaseRepository creditPurchaseRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final FlutterwaveService flutterwaveService;
    private final QueueService queueService;
    private final RandomNumberGenerator randomNumberGenerator;
    private final ObjectMapper objectMapper;
    private final ExchangeRateService exchangeRateService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.admin.superadmin-email:hello@tmag.health}")
    private String superadminEmail;

    @Value("${app.payment.flutterwave.onboarding-callback-url:#{null}}")
    private String onboardingCallbackUrl;

    public CompanyOnboardingService(
            CompanyOnboardingRepository onboardingRepository,
            CompanyRepository companyRepository,
            CreditPlanRepository userCreditPlanRepository,
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            CompanyUserRepository companyUserRepository,
            CreditRepository creditRepository,
            CreditPurchaseRepository creditPurchaseRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            FlutterwaveService flutterwaveService,
            QueueService queueService,
            RandomNumberGenerator randomNumberGenerator,
            ObjectMapper objectMapper,
            ExchangeRateService exchangeRateService) {
        this.onboardingRepository = onboardingRepository;
        this.companyRepository = companyRepository;
        this.userCreditPlanRepository = userCreditPlanRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.companyUserRepository = companyUserRepository;
        this.creditRepository = creditRepository;
        this.creditPurchaseRepository = creditPurchaseRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.flutterwaveService = flutterwaveService;
        this.queueService = queueService;
        this.randomNumberGenerator = randomNumberGenerator;
        this.objectMapper = objectMapper;
        this.exchangeRateService = exchangeRateService;
    }

    public CompanyOnboardingResponse submitOnboarding(CompanyOnboardingSubmitRequest req) {
        // Validate plan code
        CreditPlanCode planCode;
        try {
            planCode = CreditPlanCode.valueOf(req.selectedPlanCode().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid plan code: " + req.selectedPlanCode());
        }
        userCreditPlanRepository.findByCode(planCode)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + req.selectedPlanCode()));

        if (req.teamMembers() == null || req.teamMembers().isEmpty()) {
            throw new IllegalArgumentException("At least one team member is required");
        }

        int creditCount = req.creditCount() != null ? req.creditCount() : 0;

        BillingCurrency currency;
        try {
            currency = BillingCurrency.valueOf(req.billingCurrency().toUpperCase());
        } catch (IllegalArgumentException e) {
            currency = BillingCurrency.USD;
        }

        String teamMembersJson;
        try {
            teamMembersJson = objectMapper.writeValueAsString(req.teamMembers());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid team members data");
        }

        String txRef = flutterwaveService.generateTransactionReference();

        CompanyOnboardingEntity entity = new CompanyOnboardingEntity();
        entity.setCompanyName(req.companyName());
        entity.setIndustry(req.industry());
        entity.setContactEmail(req.contactEmail());
        entity.setContactPhone(req.contactPhone());
        entity.setWebsite(req.website());
        entity.setBillingCurrency(currency);
        entity.setSelectedPlanCode(planCode.name());
        entity.setCreditCount(creditCount);
        entity.setSampleRequest(req.sampleRequest());
        entity.setTeamMembers(teamMembersJson);
        entity.setTxRef(txRef);
        entity.setStatus(OnboardingStatus.PENDING_PAYMENT);
        entity.setPaymentStatus(OnboardingPaymentStatus.PENDING);

        entity = onboardingRepository.save(entity);

        logger.info("Company onboarding request created: id={}, company={}, plan={}, credits={}",
                entity.getId(), entity.getCompanyName(), entity.getSelectedPlanCode(), creditCount);

        return toResponse(entity);
    }

    public Map<String, Object> initiatePayment(Long onboardingId) {
        CompanyOnboardingEntity entity = onboardingRepository.findById(onboardingId)
                .orElseThrow(() -> new NoSuchElementException("Onboarding request not found"));

        if (entity.getStatus() != OnboardingStatus.PENDING_PAYMENT) {
            throw new IllegalArgumentException("Onboarding request is not in pending payment status");
        }

        CreditPlanCode planCode = CreditPlanCode.valueOf(entity.getSelectedPlanCode());
        CreditPlan plan = userCreditPlanRepository.findByCode(planCode)
                .orElseThrow(() -> new NoSuchElementException("Plan not found: " + entity.getSelectedPlanCode()));

        int creditCount = entity.getCreditCount() != null ? entity.getCreditCount() : 0;
        String currencyCode = entity.getBillingCurrency().name();
        BigDecimal pricePerCredit = exchangeRateService.convertFromUsd(plan.getBasePriceUsd(), currencyCode);
        BigDecimal price = pricePerCredit.multiply(BigDecimal.valueOf(creditCount));

        // Essential plan (free) — skip payment if price is zero
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            entity.setPaymentAmount(BigDecimal.ZERO);
            entity.setPaymentCurrency(currencyCode);
            entity.setPaymentStatus(OnboardingPaymentStatus.COMPLETED);
            entity.setStatus(OnboardingStatus.PENDING_APPROVAL);
            entity.setPaidAt(LocalDateTime.now());
            onboardingRepository.save(entity);

            return Map.of(
                    "txRef", entity.getTxRef(),
                    "paymentLink", "",
                    "amount", BigDecimal.ZERO,
                    "currency", currencyCode,
                    "free", true);
        }

        entity.setPaymentAmount(price);
        entity.setPaymentCurrency(currencyCode);

        String callbackUrl = onboardingCallbackUrl != null
                ? onboardingCallbackUrl
                : frontendUrl + "/company-onboarding/callback";

        FlutterwavePaymentRequest paymentRequest = new FlutterwavePaymentRequest(
                price,
                currencyCode,
                entity.getContactEmail(),
                entity.getCompanyName(),
                "TMAG " + plan.getDisplayName() + " Plan - " + creditCount + " credits for " + entity.getCompanyName(),
                entity.getTxRef(),
                entity.getContactPhone(),
                callbackUrl,
                creditCount,
                planCode.name(),
                null,
                null);

        FlutterwavePaymentResponse response = flutterwaveService.initiatePayment(paymentRequest);

        if (response.success() && response.paymentLink() != null) {
            logger.info("Onboarding payment initiated: id={}, txRef={}, amount={}, currency={}",
                    entity.getId(), entity.getTxRef(), price, currencyCode);
            onboardingRepository.save(entity);

            return Map.of(
                    "txRef", entity.getTxRef(),
                    "paymentLink", response.paymentLink(),
                    "amount", price,
                    "currency", currencyCode);
        } else {
            throw new RuntimeException("Failed to initiate payment: " + response.message());
        }
    }

    @Transactional
    public CompanyOnboardingResponse verifyPayment(String txRef, String transactionId) {
        CompanyOnboardingEntity entity = onboardingRepository.findByTxRef(txRef)
                .orElseThrow(() -> new NoSuchElementException("Onboarding request not found with txRef: " + txRef));

        if (entity.getPaymentStatus() == OnboardingPaymentStatus.COMPLETED) {
            return toResponse(entity);
        }

        FlutterwavePaymentResponse verification;
        if (transactionId != null && !transactionId.isBlank()) {
            verification = flutterwaveService.verifyTransaction(transactionId);
        } else {
            verification = flutterwaveService.verifyTransactionByReference(txRef);
        }

        if (verification.success() && "successful".equalsIgnoreCase(verification.status())) {
            entity.setPaymentStatus(OnboardingPaymentStatus.COMPLETED);
            entity.setStatus(OnboardingStatus.PENDING_APPROVAL);
            entity.setPaidAt(LocalDateTime.now());
            entity.setFlwRef(verification.flwRef());
            if (verification.amount() != null) {
                entity.setPaymentAmount(verification.amount());
            }

            // Save entity status to COMPLETED first so concurrent/duplicate callbacks
            // hit the COMPLETED guard above and return early.
            onboardingRepository.saveAndFlush(entity);

            // Only create the CreditPurchase ledger entry if it doesn't already exist
            // (guards against double-redirect / race condition from Flutterwave).
            if (creditPurchaseRepository.findByTxRef(txRef).isEmpty()) {
                int credits = entity.getCreditCount() != null ? entity.getCreditCount() : 0;
                BigDecimal totalAmount = entity.getPaymentAmount() != null ? entity.getPaymentAmount() : BigDecimal.ZERO;
                BigDecimal ppc = credits > 0
                        ? totalAmount.divide(BigDecimal.valueOf(credits), 2, java.math.RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                CreditPurchase purchase = new CreditPurchase();
                purchase.setTxRef(txRef);
                purchase.setCreditsPurchased(credits);
                purchase.setCurrency(entity.getBillingCurrency());
                purchase.setCurrencySymbol(exchangeRateService.getCurrencySymbol(entity.getBillingCurrency().name()));
                purchase.setPricePerCredit(ppc);
                purchase.setAmount(totalAmount);
                purchase.setAmountPaid(verification.amount() != null ? verification.amount() : totalAmount);
                purchase.setStatus("completed");
                purchase.setPaidAt(LocalDateTime.now());
                purchase.setFlutterwaveStatus(verification.status());
                purchase.setFlwRef(verification.flwRef());
                creditPurchaseRepository.save(purchase);
            }

            queueService.dispatch(JobType.EMAIL_GENERIC, Map.of(
                    "to", superadminEmail,
                    "subject", "New Company Registration Pending Approval - " + entity.getCompanyName(),
                    "variables", Map.of(
                            "firstName", "Admin",
                            "content", "<p>A new company registration is pending your approval.</p>"
                                    + "<p><strong>Company:</strong> " + entity.getCompanyName() + "</p>"
                                    + "<p><strong>Plan:</strong> " + entity.getSelectedPlanCode() + "</p>"
                                    + "<p><strong>Credits:</strong> " + (entity.getCreditCount() != null ? entity.getCreditCount() : 0) + "</p>"
                                    + "<p><strong>Amount Paid:</strong> " + entity.getPaymentAmount() + " " + entity.getPaymentCurrency() + "</p>"
                                    + "<p><strong>Contact:</strong> " + entity.getContactEmail() + "</p>"
                                    + "<p><a href=\"" + frontendUrl.replace("localhost:3000", "localhost:3002")
                                    + "/admin/company-registrations\">Review and Approve</a></p>")));

            logger.info("Onboarding payment verified: id={}, txRef={}, status=pending_approval",
                    entity.getId(), txRef);
        } else {
            entity.setPaymentStatus(OnboardingPaymentStatus.FAILED);
            logger.warn("Onboarding payment verification failed: id={}, txRef={}", entity.getId(), txRef);
            onboardingRepository.save(entity);
        }

        return toResponse(entity);
    }

    public CompanyOnboardingResponse getStatus(Long id) {
        CompanyOnboardingEntity entity = onboardingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Onboarding request not found"));
        return toResponse(entity);
    }

    // ============ ADMIN ENDPOINTS ============

    public List<CompanyOnboardingResponse> getRequestsByStatus(OnboardingStatus status) {
        List<CompanyOnboardingEntity> entities = status != null
                ? onboardingRepository.findByStatusOrderByCreatedAtDesc(status)
                : onboardingRepository.findAll();
        return entities.stream().map(this::toResponse).toList();
    }

    public CompanyOnboardingResponse getRequestById(Long id) {
        CompanyOnboardingEntity entity = onboardingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Onboarding request not found"));
        return toResponse(entity);
    }

    @Transactional
    public CompanyOnboardingResponse approveRequest(Long id, String adminEmail) {
        CompanyOnboardingEntity entity = onboardingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Onboarding request not found"));

        if (entity.getStatus() != OnboardingStatus.PENDING_APPROVAL) {
            throw new IllegalArgumentException("Request is not in pending approval status");
        }

        Company company = createCompanyFromRequest(entity);

        entity.setStatus(OnboardingStatus.APPROVED);
        entity.setReviewedBy(adminEmail);
        entity.setReviewedAt(LocalDateTime.now());
        entity.setCreatedCompanyId(company.getId());

        int credits = entity.getCreditCount() != null ? entity.getCreditCount() : 0;

        queueService.dispatch(JobType.EMAIL_GENERIC, Map.of(
                "to", entity.getContactEmail(),
                "subject", "Your TMAG Company Registration Has Been Approved!",
                "variables", Map.of(
                        "firstName", "there",
                        "content", "<p>Congratulations! Your company <strong>\"" + entity.getCompanyName()
                                + "\"</strong> has been approved on TMAG.</p>"
                                + "<p>Your team members will receive invitation emails shortly.</p>"
                                + "<p><strong>Plan:</strong> " + entity.getSelectedPlanCode() + "</p>"
                                + "<p><strong>Credits:</strong> " + credits + " credits</p>"
                                + "<p>If you have any questions, please contact our support team.</p>")));

        logger.info("Onboarding request approved: id={}, company={}, admin={}",
                id, entity.getCompanyName(), adminEmail);

        return toResponse(entity);
    }

    @Transactional
    public CompanyOnboardingResponse rejectRequest(Long id, String reason, String adminEmail) {
        CompanyOnboardingEntity entity = onboardingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Onboarding request not found"));

        if (entity.getStatus() != OnboardingStatus.PENDING_APPROVAL) {
            throw new IllegalArgumentException("Request is not in pending approval status");
        }

        entity.setStatus(OnboardingStatus.REJECTED);
        entity.setRejectionReason(reason);
        entity.setReviewedBy(adminEmail);
        entity.setReviewedAt(LocalDateTime.now());

        queueService.dispatch(JobType.EMAIL_GENERIC, Map.of(
                "to", entity.getContactEmail(),
                "subject", "TMAG Company Registration Update",
                "variables", Map.of(
                        "firstName", "there",
                        "content", "<p>We regret to inform you that your company registration for <strong>\""
                                + entity.getCompanyName() + "\"</strong> has not been approved at this time.</p>"
                                + "<p><strong>Reason:</strong> " + (reason != null ? reason : "No reason provided") + "</p>"
                                + "<p>If you believe this is an error or would like to reapply, "
                                + "please contact our support team.</p>")));

        logger.info("Onboarding request rejected: id={}, company={}, admin={}, reason={}",
                id, entity.getCompanyName(), adminEmail, reason);

        return toResponse(entity);
    }

    // ============ PRIVATE METHODS ============

    private Company createCompanyFromRequest(CompanyOnboardingEntity entity) {
        String companyCode = String.format("TMA-%s", randomNumberGenerator.generateNumber());

        CreditPlanCode planCode = CreditPlanCode.valueOf(entity.getSelectedPlanCode());
        CreditPlan plan = userCreditPlanRepository.findByCode(planCode)
                .orElseThrow(() -> new NoSuchElementException("Plan not found: " + entity.getSelectedPlanCode()));

        int creditCount = entity.getCreditCount() != null ? entity.getCreditCount() : 0;

        Company company = new Company();
        company.setName(entity.getCompanyName());
        company.setIndustry(entity.getIndustry());
        company.setWebsite(entity.getWebsite());
        company.setContactEmail(entity.getContactEmail());
        company.setContactPhone(entity.getContactPhone());
        company.setCompanyCode(companyCode);
        company.setBillingStatus(BillingStatus.ACTIVE);
        company.setTier(Tier.STANDARD);
        company.setBillingCurrency(entity.getBillingCurrency());
        company.setCreditPlan(plan);
        company.setPlan(plan.getCode().name());
        company.setTotalCredits(creditCount);
        company.setUsedCredits(0);

        List<TeamMemberRequest> teamMembers = parseTeamMembers(entity.getTeamMembers());
        company.setEmployeeCount(teamMembers.size());

        company = companyRepository.save(company);

        logger.info("Company created from onboarding: id={}, code={}, plan={}, credits={}",
                company.getId(), companyCode, planCode, creditCount);

        int creditsPerMember = teamMembers.size() > 0 ? creditCount / teamMembers.size() : 0;
        int totalCreditsAllocated = 0;

        for (TeamMemberRequest member : teamMembers) {
            if (userRepository.findByEmail(member.email()).isPresent()) {
                logger.warn("User already exists with email: {}, skipping", member.email());
                continue;
            }

            String[] nameParts = member.name().trim().split("\\s+", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";

            String roleName;
            String department;
            if ("admin".equalsIgnoreCase(member.role())) {
                roleName = "Administrator";
                department = "Administration";
            } else {
                roleName = "Hr";
                department = "HR";
            }

            Role role = roleRepository.findByName(roleName).orElse(
                    roleRepository.findByName("Individual").orElse(null));

            String invitationToken = generateToken(32);

            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setName(member.name());
            user.setUsername(member.email());
            user.setEmail(member.email());
            user.setPassword(passwordEncoder.encode(invitationToken));
            user.setOnboardingStage(0);
            user.setOnboarded(false);
            user.setVerified(false);
            user.setMustChangePassword(true);
            user.setInvitationToken(invitationToken);
            user.setInvitationTokenExpiry(LocalDateTime.now().plusDays(7));
            user.setType("COMPANY");
            user.setCredits(creditsPerMember);
            user.setRole(role);
            user.setBillingCurrency(entity.getBillingCurrency());
            user.setCreditPlan(plan);
            user = userRepository.save(user);

            Employee employee = new Employee();
            employee.setName(member.name());
            employee.setEmail(member.email());
            employee.setDepartment(department);
            employee.setCreditsAllocated(creditsPerMember);
            employee.setCreditsUsed(0);
            employee.setPlansGenerated(0);
            employee.setStatus("active");
            employee.setCompany(company);
            employee.setUser(user);
            employeeRepository.save(employee);

            CompanyUser companyUser = new CompanyUser();
            companyUser.setRole(roleName);
            companyUser.setCreditsAllocated(creditsPerMember);
            companyUser.setCreditsUsed(0);
            companyUser.setCompany(company);
            companyUser.setUser(user);
            companyUserRepository.save(companyUser);

            if (creditsPerMember > 0) {
                Credit credit = new Credit();
                credit.setAmount(creditsPerMember);
                credit.setType("signup_allocation");
                credit.setReference("Onboarding signup: " + member.email());
                credit.setBalanceAfter(creditsPerMember);
                credit.setUser(user);
                credit.setCompany(company);
                creditRepository.save(credit);
            }

            totalCreditsAllocated += creditsPerMember;

            String inviteLink = frontendUrl + "/accept-invitation?token=" + invitationToken;
            queueService.dispatch(JobType.EMAIL_EMPLOYEE_INVITATION, Map.of(
                    "to", member.email(),
                    "subject", "You're invited to join " + company.getName() + " on TMAG",
                    "variables", Map.of(
                            "firstName", firstName,
                            "companyName", company.getName(),
                            "role", roleName,
                            "link", inviteLink)));

            logger.info("Team member created: email={}, role={}, company={}",
                    member.email(), roleName, company.getName());
        }

        company.setUsedCredits(totalCreditsAllocated);
        companyRepository.save(company);

        // Only create the signup CreditPurchase if it doesn't already exist
        // (verifyPayment may have already inserted one).
        if (creditPurchaseRepository.findByTxRef(entity.getTxRef()).isEmpty()) {
            BigDecimal totalAmt = entity.getPaymentAmount() != null ? entity.getPaymentAmount() : BigDecimal.ZERO;
            BigDecimal ppc = creditCount > 0
                    ? totalAmt.divide(BigDecimal.valueOf(creditCount), 2, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            CreditPurchase purchase = new CreditPurchase();
            purchase.setTxRef(entity.getTxRef());
            purchase.setCreditsPurchased(creditCount);
            purchase.setCurrency(entity.getBillingCurrency());
            purchase.setCurrencySymbol(exchangeRateService.getCurrencySymbol(entity.getBillingCurrency().name()));
            purchase.setPricePerCredit(ppc);
            purchase.setAmount(totalAmt);
            purchase.setAmountPaid(totalAmt);
            purchase.setStatus("completed");
            purchase.setPaidAt(entity.getPaidAt());
            purchase.setCompanyId(company.getId());
            creditPurchaseRepository.save(purchase);
        }

        if (creditCount > 0) {
            Credit companyCredit = new Credit();
            companyCredit.setAmount(creditCount);
            companyCredit.setType("signup");
            companyCredit.setReference("Onboarding: " + entity.getTxRef());
            companyCredit.setBalanceAfter(creditCount);
            companyCredit.setCompany(company);
            creditRepository.save(companyCredit);
        }

        return company;
    }

    private List<TeamMemberRequest> parseTeamMembers(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<TeamMemberRequest>>() {});
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse team members JSON: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private String generateToken(int byteLength) {
        byte[] bytes = new byte[byteLength];
        new SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private CompanyOnboardingResponse toResponse(CompanyOnboardingEntity entity) {
        List<CompanyOnboardingResponse.TeamMemberResponse> teamMembers = new ArrayList<>();
        List<TeamMemberRequest> parsed = parseTeamMembers(entity.getTeamMembers());
        for (TeamMemberRequest tm : parsed) {
            teamMembers.add(new CompanyOnboardingResponse.TeamMemberResponse(tm.name(), tm.email(), tm.role()));
        }

        return new CompanyOnboardingResponse(
                entity.getId(),
                entity.getCompanyName(),
                entity.getIndustry(),
                entity.getContactEmail(),
                entity.getContactPhone(),
                entity.getWebsite(),
                entity.getBillingCurrency() != null ? entity.getBillingCurrency().name() : "USD",
                entity.getSelectedPlanCode(),
                entity.getCreditCount(),
                entity.getSampleRequest(),
                teamMembers,
                entity.getTxRef(),
                entity.getPaymentStatus() != null ? entity.getPaymentStatus().name().toLowerCase() : "pending",
                entity.getPaymentAmount(),
                entity.getPaymentCurrency(),
                entity.getStatus() != null ? entity.getStatus().name().toLowerCase() : "pending_payment",
                entity.getRejectionReason(),
                entity.getReviewedBy(),
                entity.getReviewedAt(),
                entity.getCreatedCompanyId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
