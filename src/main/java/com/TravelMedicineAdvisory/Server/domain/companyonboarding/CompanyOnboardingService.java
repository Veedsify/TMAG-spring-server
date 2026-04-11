package com.TravelMedicineAdvisory.Server.domain.companyonboarding;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

import com.TravelMedicineAdvisory.Server.core.payment.FlutterwavePaymentRequest;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwavePaymentResponse;
import com.TravelMedicineAdvisory.Server.core.payment.FlutterwaveService;
import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.core.utils.RandomNumberGenerator;
import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import com.TravelMedicineAdvisory.Server.domain.company.BillingStatus;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.company.Tier;
import com.TravelMedicineAdvisory.Server.domain.companyonboarding.CompanyOnboardingSubmitRequest.TeamMemberRequest;
import com.TravelMedicineAdvisory.Server.domain.companyplan.PlanCode;
import com.TravelMedicineAdvisory.Server.domain.companyplan.PlanEntity;
import com.TravelMedicineAdvisory.Server.domain.companyplan.PlanRepository;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class CompanyOnboardingService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyOnboardingService.class);

    private final CompanyOnboardingRepository onboardingRepository;
    private final CompanyRepository companyRepository;
    private final PlanRepository planRepository;
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

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.admin.superadmin-email:hello@tmag.health}")
    private String superadminEmail;

    @Value("${app.payment.flutterwave.onboarding-callback-url:#{null}}")
    private String onboardingCallbackUrl;

    public CompanyOnboardingService(
            CompanyOnboardingRepository onboardingRepository,
            CompanyRepository companyRepository,
            PlanRepository planRepository,
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
            ObjectMapper objectMapper) {
        this.onboardingRepository = onboardingRepository;
        this.companyRepository = companyRepository;
        this.planRepository = planRepository;
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
    }

    public CompanyOnboardingResponse submitOnboarding(CompanyOnboardingSubmitRequest req) {
        // Validate plan exists
        PlanCode planCode;
        try {
            planCode = PlanCode.valueOf(req.selectedPlanCode().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid plan code: " + req.selectedPlanCode());
        }
        planRepository.findByCode(planCode)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + req.selectedPlanCode()));

        // Validate at least one team member
        if (req.teamMembers() == null || req.teamMembers().isEmpty()) {
            throw new IllegalArgumentException("At least one team member is required");
        }

        // Validate billing currency
        BillingCurrency currency;
        try {
            currency = BillingCurrency.valueOf(req.billingCurrency().toUpperCase());
        } catch (IllegalArgumentException e) {
            currency = BillingCurrency.USD;
        }

        // Serialize team members
        String teamMembersJson;
        try {
            teamMembersJson = objectMapper.writeValueAsString(req.teamMembers());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid team members data");
        }

        // Generate txRef
        String txRef = flutterwaveService.generateTransactionReference();

        CompanyOnboardingEntity entity = new CompanyOnboardingEntity();
        entity.setCompanyName(req.companyName());
        entity.setIndustry(req.industry());
        entity.setContactEmail(req.contactEmail());
        entity.setContactPhone(req.contactPhone());
        entity.setWebsite(req.website());
        entity.setBillingCurrency(currency);
        entity.setSelectedPlanCode(planCode.name());
        entity.setSampleRequest(req.sampleRequest());
        entity.setTeamMembers(teamMembersJson);
        entity.setTxRef(txRef);
        entity.setStatus(OnboardingStatus.PENDING_PAYMENT);
        entity.setPaymentStatus(OnboardingPaymentStatus.PENDING);

        entity = onboardingRepository.save(entity);

        logger.info("Company onboarding request created: id={}, company={}, plan={}",
                entity.getId(), entity.getCompanyName(), entity.getSelectedPlanCode());

        return toResponse(entity);
    }

    public Map<String, Object> initiatePayment(Long onboardingId) {
        CompanyOnboardingEntity entity = onboardingRepository.findById(onboardingId)
                .orElseThrow(() -> new NoSuchElementException("Onboarding request not found"));

        if (entity.getStatus() != OnboardingStatus.PENDING_PAYMENT) {
            throw new IllegalArgumentException("Onboarding request is not in pending payment status");
        }

        // Get plan and price
        PlanCode planCode = PlanCode.valueOf(entity.getSelectedPlanCode());
        PlanEntity plan = planRepository.findByCode(planCode)
                .orElseThrow(() -> new NoSuchElementException("Plan not found"));

        BigDecimal price = getPriceForCurrency(plan, entity.getBillingCurrency());
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("No price configured for plan " + planCode + " in " + entity.getBillingCurrency());
        }

        entity.setPaymentAmount(price);
        entity.setPaymentCurrency(entity.getBillingCurrency().name());

        // Build callback URL - Flutterwave appends ?tx_ref=...&status=...&transaction_id=...
        String callbackUrl = onboardingCallbackUrl != null
                ? onboardingCallbackUrl
                : frontendUrl + "/company-onboarding/callback";

        FlutterwavePaymentRequest paymentRequest = new FlutterwavePaymentRequest(
                price,
                entity.getBillingCurrency().name(),
                entity.getContactEmail(),
                entity.getCompanyName(),
                "TMAG Company Plan - " + plan.getDisplayName() + " for " + entity.getCompanyName(),
                entity.getTxRef(),
                entity.getContactPhone(),
                callbackUrl,
                1,
                planCode.name(),
                null,
                null);

        FlutterwavePaymentResponse response = flutterwaveService.initiatePayment(paymentRequest);

        if (response.success() && response.paymentLink() != null) {
            logger.info("Onboarding payment initiated: id={}, txRef={}, amount={}, currency={}",
                    entity.getId(), entity.getTxRef(), price, entity.getBillingCurrency());

            return Map.of(
                    "txRef", entity.getTxRef(),
                    "paymentLink", response.paymentLink(),
                    "amount", price,
                    "currency", entity.getBillingCurrency().name());
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

            // Record credit purchase
            CreditPurchase purchase = new CreditPurchase();
            purchase.setTxRef(txRef);
            purchase.setCreditsPurchased(getPlanCredits(entity.getSelectedPlanCode()));
            purchase.setCurrency(entity.getBillingCurrency());
            purchase.setAmount(entity.getPaymentAmount());
            purchase.setAmountPaid(verification.amount() != null ? verification.amount() : entity.getPaymentAmount());
            purchase.setStatus("completed");
            purchase.setPaidAt(LocalDateTime.now());
            purchase.setFlutterwaveStatus(verification.status());
            purchase.setFlwRef(verification.flwRef());
            creditPurchaseRepository.save(purchase);

            // Notify superadmin
            queueService.dispatch(JobType.EMAIL_GENERIC, Map.of(
                    "to", superadminEmail,
                    "subject", "New Company Registration Pending Approval - " + entity.getCompanyName(),
                    "variables", Map.of(
                            "firstName", "Admin",
                            "content", "<p>A new company registration is pending your approval.</p>"
                                    + "<p><strong>Company:</strong> " + entity.getCompanyName() + "</p>"
                                    + "<p><strong>Plan:</strong> " + entity.getSelectedPlanCode() + "</p>"
                                    + "<p><strong>Amount Paid:</strong> " + entity.getPaymentAmount() + " " + entity.getPaymentCurrency() + "</p>"
                                    + "<p><strong>Contact:</strong> " + entity.getContactEmail() + "</p>"
                                    + "<p><a href=\"" + frontendUrl.replace("localhost:3000", "localhost:3002")
                                    + "/admin/company-registrations\">Review and Approve</a></p>")));

            logger.info("Onboarding payment verified: id={}, txRef={}, status=pending_approval",
                    entity.getId(), txRef);
        } else {
            entity.setPaymentStatus(OnboardingPaymentStatus.FAILED);
            logger.warn("Onboarding payment verification failed: id={}, txRef={}", entity.getId(), txRef);
        }

        onboardingRepository.save(entity);
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

        // Create company and all related records
        Company company = createCompanyFromRequest(entity);

        entity.setStatus(OnboardingStatus.APPROVED);
        entity.setReviewedBy(adminEmail);
        entity.setReviewedAt(LocalDateTime.now());
        entity.setCreatedCompanyId(company.getId());

        // Send approval notification to contact email
        queueService.dispatch(JobType.EMAIL_GENERIC, Map.of(
                "to", entity.getContactEmail(),
                "subject", "Your TMAG Company Registration Has Been Approved!",
                "variables", Map.of(
                        "firstName", "there",
                        "content", "<p>Congratulations! Your company <strong>\"" + entity.getCompanyName()
                                + "\"</strong> has been approved on TMAG.</p>"
                                + "<p>Your team members will receive invitation emails shortly to access their dashboards.</p>"
                                + "<p><strong>Plan:</strong> " + entity.getSelectedPlanCode() + "</p>"
                                + "<p><strong>Credits:</strong> " + getPlanCredits(entity.getSelectedPlanCode()) + " signup credits</p>"
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

        // Notify the company contact
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
        // 1. Create Company
        String companyCode = String.format("TMA-%s", randomNumberGenerator.generateNumber());

        PlanCode planCode = PlanCode.valueOf(entity.getSelectedPlanCode());
        PlanEntity plan = planRepository.findByCode(planCode)
                .orElseThrow(() -> new NoSuchElementException("Plan not found: " + entity.getSelectedPlanCode()));

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
        company.setActivePlan(plan);
        company.setPlan(plan.getCode().name());
        company.setTotalCredits(plan.getSignupCredits());
        company.setUsedCredits(0);

        // Parse team members
        List<TeamMemberRequest> teamMembers = parseTeamMembers(entity.getTeamMembers());
        company.setEmployeeCount(teamMembers.size());

        company = companyRepository.save(company);

        logger.info("Company created from onboarding: id={}, code={}, plan={}",
                company.getId(), companyCode, planCode);

        // 2. Create users for each team member
        int creditsPerMember = teamMembers.size() > 0 ? plan.getSignupCredits() / teamMembers.size() : 0;
        int totalCreditsAllocated = 0;

        for (TeamMemberRequest member : teamMembers) {
            // Skip if user already exists
            if (userRepository.findByEmail(member.email()).isPresent()) {
                logger.warn("User already exists with email: {}, skipping", member.email());
                continue;
            }

            String[] nameParts = member.name().trim().split("\\s+", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";

            // Determine role
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

            // Generate invitation token
            String invitationToken = generateToken(32);

            // Create User
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
            user = userRepository.save(user);

            // Create Employee
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

            // Create CompanyUser
            CompanyUser companyUser = new CompanyUser();
            companyUser.setRole(roleName);
            companyUser.setCreditsAllocated(creditsPerMember);
            companyUser.setCreditsUsed(0);
            companyUser.setCompany(company);
            companyUser.setUser(user);
            companyUserRepository.save(companyUser);

            // Credit ledger entry
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

            // Queue invitation email
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

        // Update company used credits to reflect allocations
        company.setUsedCredits(totalCreditsAllocated);
        companyRepository.save(company);

        // Record signup credit purchase
        CreditPurchase purchase = new CreditPurchase();
        purchase.setTxRef(entity.getTxRef());
        purchase.setCreditsPurchased(plan.getSignupCredits());
        purchase.setCurrency(entity.getBillingCurrency());
        purchase.setAmount(entity.getPaymentAmount());
        purchase.setAmountPaid(entity.getPaymentAmount());
        purchase.setStatus("completed");
        purchase.setPaidAt(entity.getPaidAt());
        purchase.setCompanyId(company.getId());
        creditPurchaseRepository.save(purchase);

        // Credit ledger entry for company
        Credit companyCredit = new Credit();
        companyCredit.setAmount(plan.getSignupCredits());
        companyCredit.setType("signup");
        companyCredit.setReference("Onboarding: " + entity.getTxRef());
        companyCredit.setBalanceAfter(plan.getSignupCredits());
        companyCredit.setCompany(company);
        creditRepository.save(companyCredit);

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

    private BigDecimal getPriceForCurrency(PlanEntity plan, BillingCurrency currency) {
        return switch (currency) {
            case USD -> plan.getPriceUsd();
            case NGN -> plan.getPriceNgn();
            case EUR -> plan.getPriceEur();
            case GBP -> plan.getPriceGbp();
        };
    }

    private Integer getPlanCredits(String planCodeStr) {
        try {
            PlanCode planCode = PlanCode.valueOf(planCodeStr);
            return planRepository.findByCode(planCode)
                    .map(PlanEntity::getSignupCredits)
                    .orElse(0);
        } catch (IllegalArgumentException e) {
            return 0;
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
