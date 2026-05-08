package com.TravelMedicineAdvisory.Server.domain.admin.affiliate;

import com.TravelMedicineAdvisory.Server.core.email.EmailService;
import com.TravelMedicineAdvisory.Server.core.email.EmailTemplates;
import com.TravelMedicineAdvisory.Server.domain.affiliate.AffiliateApplication;
import com.TravelMedicineAdvisory.Server.domain.affiliate.AffiliateApplicationRepository;
import com.TravelMedicineAdvisory.Server.domain.affiliate.AffiliateApplicationResponse;
import com.TravelMedicineAdvisory.Server.domain.affiliate.AffiliateCommissionRepository;
import com.TravelMedicineAdvisory.Server.domain.affiliate.AffiliatePayoutRepository;
import com.TravelMedicineAdvisory.Server.domain.affiliate.AffiliateProfile;
import com.TravelMedicineAdvisory.Server.domain.affiliate.AffiliateProfileRepository;
import com.TravelMedicineAdvisory.Server.domain.affiliate.AffiliateReferralRepository;
import com.TravelMedicineAdvisory.Server.domain.role.Role;
import com.TravelMedicineAdvisory.Server.domain.role.RoleRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class AdminAffiliateService {

    private final AffiliateApplicationRepository affiliateApplicationRepository;
    private final AffiliateProfileRepository affiliateProfileRepository;
    private final AffiliateCommissionRepository affiliateCommissionRepository;
    private final AffiliatePayoutRepository affiliatePayoutRepository;
    private final AffiliateReferralRepository affiliateReferralRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EmailTemplates emailTemplates;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public AdminAffiliateService(
            AffiliateApplicationRepository affiliateApplicationRepository,
            AffiliateProfileRepository affiliateProfileRepository,
            AffiliateCommissionRepository affiliateCommissionRepository,
            AffiliatePayoutRepository affiliatePayoutRepository,
            AffiliateReferralRepository affiliateReferralRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            EmailTemplates emailTemplates) {
        this.affiliateApplicationRepository = affiliateApplicationRepository;
        this.affiliateProfileRepository = affiliateProfileRepository;
        this.affiliateCommissionRepository = affiliateCommissionRepository;
        this.affiliatePayoutRepository = affiliatePayoutRepository;
        this.affiliateReferralRepository = affiliateReferralRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.emailTemplates = emailTemplates;
    }

    // -------------------------------------------------------------------------
    // Application management
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<AffiliateApplicationResponse> listAllApplications() {
        return affiliateApplicationRepository.findByDeletedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .map(AffiliateApplicationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AffiliateApplicationResponse> listApplicationsByStatus(String status) {
        return affiliateApplicationRepository.findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(status)
                .stream()
                .map(AffiliateApplicationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AffiliateApplicationResponse getApplication(Long id) {
        AffiliateApplication app = affiliateApplicationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NoSuchElementException("Application not found"));
        return AffiliateApplicationResponse.from(app);
    }

    public AdminAffiliateDetailResponse approveApplication(Long applicationId) {
        AffiliateApplication app = affiliateApplicationRepository.findByIdAndDeletedAtIsNull(applicationId)
                .orElseThrow(() -> new NoSuchElementException("Application not found"));

        if (!"pending".equalsIgnoreCase(app.getStatus()) && !"info_requested".equalsIgnoreCase(app.getStatus())) {
            throw new IllegalStateException("Application is not in a reviewable state: " + app.getStatus());
        }

        // Check if user already exists
        if (userRepository.findByEmailIgnoreCase(app.getEmail()).isPresent()) {
            throw new IllegalStateException("A user with this email already exists");
        }

        // Generate temp password
        String tempPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        // Parse name
        String[] nameParts = app.getFullName().trim().split("\\s+", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        // Find AFFILIATE role
        Role affiliateRole = roleRepository.findByName("AFFILIATE")
                .orElseThrow(() -> new NoSuchElementException("AFFILIATE role not found"));

        // Create user
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(app.getEmail());
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setRole(affiliateRole);
        user.setIsActive(true);
        user.setVerified(true);
        user.setMustChangePassword(true);
        user.setOnboarded(false);
        User savedUser = userRepository.save(user);

        // Create affiliate profile
        AffiliateProfile profile = new AffiliateProfile();
        profile.setUser(savedUser);
        profile.setReferralCode(generateReferralCode(savedUser));
        profile.setCommissionRate(BigDecimal.valueOf(10));
        profile.setDiscountRate(BigDecimal.valueOf(2));
        profile.setStatus("active");
        AffiliateProfile savedProfile = affiliateProfileRepository.save(profile);

        // Update application
        app.setStatus("approved");
        app.setApprovedAt(LocalDateTime.now());
        affiliateApplicationRepository.save(app);

        // Send welcome email
        try {
            String loginUrl = trimTrailingSlash(frontendUrl) + "/login";
            emailService.sendHtmlEmail(
                    app.getEmail(),
                    "Welcome to TMAG Affiliate Program",
                    emailTemplates.affiliateWelcome(firstName, app.getEmail(), tempPassword, loginUrl)
            );
        } catch (Exception e) {
            // Log but don't fail the approval
        }

        return AdminAffiliateDetailResponse.from(savedProfile, List.of(), List.of(), List.of());
    }

    public AffiliateApplicationResponse rejectApplication(Long applicationId, String reason) {
        AffiliateApplication app = affiliateApplicationRepository.findByIdAndDeletedAtIsNull(applicationId)
                .orElseThrow(() -> new NoSuchElementException("Application not found"));

        if (!"pending".equalsIgnoreCase(app.getStatus()) && !"info_requested".equalsIgnoreCase(app.getStatus())) {
            throw new IllegalStateException("Application is not in a reviewable state: " + app.getStatus());
        }

        app.setStatus("rejected");
        app.setRejectionReason(reason);
        affiliateApplicationRepository.save(app);

        // Send rejection email
        try {
            String[] nameParts = app.getFullName().trim().split("\\s+", 2);
            String firstName = nameParts[0];
            emailService.sendHtmlEmail(
                    app.getEmail(),
                    "Your TMAG Affiliate Application",
                    emailTemplates.affiliateRejection(firstName, reason)
            );
        } catch (Exception e) {
            // Log but don't fail
        }

        return AffiliateApplicationResponse.from(app);
    }

    public AffiliateApplicationResponse requestInfo(Long applicationId, String notes) {
        AffiliateApplication app = affiliateApplicationRepository.findByIdAndDeletedAtIsNull(applicationId)
                .orElseThrow(() -> new NoSuchElementException("Application not found"));

        if (!"pending".equalsIgnoreCase(app.getStatus()) && !"info_requested".equalsIgnoreCase(app.getStatus())) {
            throw new IllegalStateException("Application is not in a reviewable state: " + app.getStatus());
        }

        app.setStatus("info_requested");
        app.setAdminNotes(notes);
        affiliateApplicationRepository.save(app);

        return AffiliateApplicationResponse.from(app);
    }

    // -------------------------------------------------------------------------
    // Affiliate management
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<AdminAffiliateListResponse> listAffiliates() {
        return affiliateProfileRepository.findByDeletedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .map(AdminAffiliateListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<AdminAffiliateListResponse> listAffiliatesPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return affiliateProfileRepository.findByDeletedAtIsNull(pageable)
                .map(AdminAffiliateListResponse::from);
    }

    @Transactional(readOnly = true)
    public AdminAffiliateStatsResponse getStats() {
        long totalActive = affiliateProfileRepository.countByStatusAndDeletedAtIsNull("active");
        long totalPending = affiliateApplicationRepository.countByStatusAndDeletedAtIsNull("pending");
        BigDecimal totalPaid = affiliateProfileRepository.sumTotalPaidOut();
        BigDecimal totalPending2 = affiliateProfileRepository.sumPendingCommission();
        BigDecimal totalRevenue = affiliateCommissionRepository.sumTotalRevenue();

        Pageable top5 = PageRequest.of(0, 5);
        List<AdminAffiliateStatsResponse.TopAffiliateItem> topAffiliates =
                affiliateProfileRepository.findTopByTotalCommissionEarned(top5)
                        .stream()
                        .map(p -> {
                            String userName = null;
                            String userEmail = null;
                            if (p.getUser() != null) {
                                userName = p.getUser().getFirstName() != null
                                        ? p.getUser().getFirstName() + (p.getUser().getLastName() != null ? " " + p.getUser().getLastName() : "")
                                        : p.getUser().getEmail();
                                userEmail = p.getUser().getEmail();
                            }
                            return new AdminAffiliateStatsResponse.TopAffiliateItem(
                                    p.getId(),
                                    userName,
                                    userEmail,
                                    p.getReferralCode(),
                                    p.getTotalCommissionEarned() != null ? p.getTotalCommissionEarned() : BigDecimal.ZERO
                            );
                        })
                        .toList();

        return new AdminAffiliateStatsResponse(
                totalActive,
                totalPending,
                totalPaid != null ? totalPaid : BigDecimal.ZERO,
                totalPending2 != null ? totalPending2 : BigDecimal.ZERO,
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                topAffiliates
        );
    }

    @Transactional(readOnly = true)
    public AdminAffiliateDetailResponse getAffiliateDetail(Long affiliateProfileId) {
        AffiliateProfile profile = affiliateProfileRepository.findByIdAndDeletedAtIsNull(affiliateProfileId)
                .orElseThrow(() -> new NoSuchElementException("Affiliate profile not found"));

        var commissions = affiliateCommissionRepository
                .findByAffiliateProfileIdAndDeletedAtIsNullOrderByCreatedAtDesc(affiliateProfileId);
        var payouts = affiliatePayoutRepository
                .findByAffiliateProfileIdAndDeletedAtIsNullOrderByRequestedAtDesc(affiliateProfileId);
        var referrals = affiliateReferralRepository
                .findByAffiliateProfileIdAndDeletedAtIsNullOrderByCreatedAtDesc(affiliateProfileId);

        return AdminAffiliateDetailResponse.from(profile, commissions, payouts, referrals);
    }

    public AdminAffiliateDetailResponse suspendAffiliate(Long affiliateProfileId) {
        AffiliateProfile profile = affiliateProfileRepository.findByIdAndDeletedAtIsNull(affiliateProfileId)
                .orElseThrow(() -> new NoSuchElementException("Affiliate profile not found"));
        profile.setStatus("suspended");
        affiliateProfileRepository.save(profile);
        return getAffiliateDetail(affiliateProfileId);
    }

    public AdminAffiliateDetailResponse activateAffiliate(Long affiliateProfileId) {
        AffiliateProfile profile = affiliateProfileRepository.findByIdAndDeletedAtIsNull(affiliateProfileId)
                .orElseThrow(() -> new NoSuchElementException("Affiliate profile not found"));
        profile.setStatus("active");
        affiliateProfileRepository.save(profile);
        return getAffiliateDetail(affiliateProfileId);
    }

    public AdminAffiliateDetailResponse updateCommissionRate(Long affiliateProfileId, BigDecimal rate) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Commission rate must be between 0 and 100");
        }
        AffiliateProfile profile = affiliateProfileRepository.findByIdAndDeletedAtIsNull(affiliateProfileId)
                .orElseThrow(() -> new NoSuchElementException("Affiliate profile not found"));
        profile.setCommissionRate(rate);
        affiliateProfileRepository.save(profile);
        return getAffiliateDetail(affiliateProfileId);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

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

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:3000";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
