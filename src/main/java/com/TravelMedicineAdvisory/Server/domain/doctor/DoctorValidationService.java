package com.TravelMedicineAdvisory.Server.domain.doctor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.TravelMedicineAdvisory.Server.core.email.EmailService;
import com.TravelMedicineAdvisory.Server.core.email.EmailTemplates;
import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.core.storage.StorageService;
import com.TravelMedicineAdvisory.Server.domain.plans.GeneratedPlan;
import com.TravelMedicineAdvisory.Server.domain.plans.GeneratedPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.role.Role;
import com.TravelMedicineAdvisory.Server.domain.role.RoleRepository;
import com.TravelMedicineAdvisory.Server.domain.role.Roles;
import com.TravelMedicineAdvisory.Server.domain.travelplan.DoctorValidationPlanProjection;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlan;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanPdfGenerator;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.user.AvatarUrlService;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.TravelMedicineAdvisory.Server.domain.usersetting.UserSetting;
import com.TravelMedicineAdvisory.Server.domain.usersetting.UserSettingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class DoctorValidationService {

    private static final Logger log = LoggerFactory.getLogger(DoctorValidationService.class);

    private final TravelPlanRepository travelPlanRepository;
    private final GeneratedPlanRepository generatedPlanRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final QueueService queueService;
    private final StorageService storageService;
    private final TravelPlanPdfGenerator pdfGenerator;
    private final EmailService emailService;
    private final EmailTemplates emailTemplates;
    private final ObjectMapper objectMapper;
    private final UserSettingService userSettingService;
    private final AvatarUrlService avatarUrlService;
    private final DoctorApplicationRepository doctorApplicationRepository;
    private final TravelPlanDoctorAssignmentRepository assignmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public DoctorValidationService(
            TravelPlanRepository travelPlanRepository,
            GeneratedPlanRepository generatedPlanRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            QueueService queueService,
            StorageService storageService,
            TravelPlanPdfGenerator pdfGenerator,
            EmailService emailService,
            EmailTemplates emailTemplates,
            ObjectMapper objectMapper,
            UserSettingService userSettingService,
            AvatarUrlService avatarUrlService,
            DoctorApplicationRepository doctorApplicationRepository,
            TravelPlanDoctorAssignmentRepository assignmentRepository,
            PasswordEncoder passwordEncoder) {
        this.travelPlanRepository = travelPlanRepository;
        this.generatedPlanRepository = generatedPlanRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.queueService = queueService;
        this.storageService = storageService;
        this.pdfGenerator = pdfGenerator;
        this.emailService = emailService;
        this.emailTemplates = emailTemplates;
        this.objectMapper = objectMapper;
        this.userSettingService = userSettingService;
        this.avatarUrlService = avatarUrlService;
        this.doctorApplicationRepository = doctorApplicationRepository;
        this.assignmentRepository = assignmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // -------------------------------------------------------------------------
    // Doctor onboarding / application
    // -------------------------------------------------------------------------

    public DoctorApplication applyToBecomeDoctor(String firstName, String lastName, String email,
            String specialty, String country, String licenseNumber, MultipartFile profilePictureFile,
            MultipartFile signatureFile, MultipartFile stampFile,
            boolean confidentialityAgreementAccepted, boolean conductAgreementAccepted) {
        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";
        if (normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (isBlank(firstName) || isBlank(lastName) || isBlank(specialty) || isBlank(country) || isBlank(licenseNumber)) {
            throw new IllegalArgumentException("Name, specialty, licence number, and country are required");
        }
        if (signatureFile == null || signatureFile.isEmpty()) {
            throw new IllegalArgumentException("Signature is required");
        }
        if (!confidentialityAgreementAccepted || !conductAgreementAccepted) {
            throw new IllegalArgumentException("Confidentiality and conduct agreements must be accepted");
        }
        doctorApplicationRepository
                .findFirstByEmailIgnoreCaseAndStatusInAndDeletedAtIsNull(normalizedEmail,
                        List.of(DoctorApplicationStatus.PENDING, DoctorApplicationStatus.APPROVED))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("You have already applied or are already a doctor");
                });

        String profilePictureUrl = null;
        if (profilePictureFile != null && !profilePictureFile.isEmpty()) {
            String profilePath = storeDoctorApplicationFile(profilePictureFile, "doctor-profile-pictures");
            profilePictureUrl = storageService.getUrl(profilePath);
        }
        String signaturePath = storeDoctorApplicationFile(signatureFile, "doctor-signatures");

        DoctorApplication application = new DoctorApplication();
        application.setFirstName(firstName != null ? firstName.trim() : "");
        application.setLastName(lastName != null ? lastName.trim() : "");
        application.setEmail(normalizedEmail);
        application.setSpecialty(specialty.trim());
        application.setCountry(country.trim());
        application.setMedicalLicenseNumber(licenseNumber.trim());
        application.setProfilePictureUrl(profilePictureUrl);
        application.setSignatureUrl(storageService.getUrl(signaturePath));
        if (stampFile != null && !stampFile.isEmpty()) {
            String stampPath = storeDoctorApplicationFile(stampFile, "doctor-stamps");
            application.setStampUrl(storageService.getUrl(stampPath));
        }
        application.setConfidentialityAgreementAccepted(confidentialityAgreementAccepted);
        application.setConductAgreementAccepted(conductAgreementAccepted);

        application.setStatus(DoctorApplicationStatus.PENDING);
        DoctorApplication saved = doctorApplicationRepository.save(application);

        log.info("Doctor application submitted: applicationId={} email={}", saved.getId(), normalizedEmail);
        return saved;
    }

    private String storeDoctorApplicationFile(MultipartFile file, String folder) {
        return storageService.storeBytes(
                readMultipartFile(file),
                folder,
                UUID.randomUUID() + "_" + file.getOriginalFilename(),
                file.getContentType());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public DoctorProfileResponse updateDoctorProfile(Long userId, String firstName, String lastName,
            String profilePictureOption, String licenseNumber, MultipartFile signatureFile,
            MultipartFile stampFile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Doctor not found"));

        UserSetting settings = userSettingService.getOrCreateByUserId(userId);

        if (settings.getDoctorApplicationStatus() != DoctorApplicationStatus.APPROVED) {
            throw new IllegalArgumentException("Only approved doctors can update their profile");
        }

        if (firstName != null && !firstName.isBlank())
            user.setFirstName(firstName);
        if (lastName != null && !lastName.isBlank())
            user.setLastName(lastName);
        if (profilePictureOption != null)
            user.setProfilePictureOption(profilePictureOption);
        if (firstName != null || lastName != null || profilePictureOption != null) {
            user.setName(user.getFirstName() + " " + user.getLastName());
            userRepository.save(user);
        }

        if (licenseNumber != null && !licenseNumber.isBlank()) {
            settings.setMedicalLicenseNumber(licenseNumber);
        }

        if (signatureFile != null && !signatureFile.isEmpty()) {
            String path = storageService.storeBytes(
                    readMultipartFile(signatureFile),
                    "doctor-signatures",
                    UUID.randomUUID() + "_" + signatureFile.getOriginalFilename(),
                    signatureFile.getContentType());
            settings.setSignatureUrl(storageService.getUrl(path));
        }

        if (stampFile != null && !stampFile.isEmpty()) {
            String path = storageService.storeBytes(
                    readMultipartFile(stampFile),
                    "doctor-stamps",
                    UUID.randomUUID() + "_" + stampFile.getOriginalFilename(),
                    stampFile.getContentType());
            settings.setStampUrl(storageService.getUrl(path));
        }

        userSettingService.updateDoctorFields(userId,
                settings.getMedicalLicenseNumber(),
                settings.getSignatureUrl(),
                settings.getStampUrl(),
                null);

        log.info("Doctor profile updated: userId={}", userId);
        return getDoctorProfile(userId);
    }

    public void onboardDoctor(Long userId, String licenseNumber, MultipartFile signatureFile) {
        UserSetting settings = userSettingService.getOrCreateByUserId(userId);

        if (settings.getDoctorApplicationStatus() != DoctorApplicationStatus.APPROVED) {
            throw new IllegalArgumentException("You must be invited/approved before onboarding");
        }

        String path = storageService.storeBytes(
                readMultipartFile(signatureFile),
                "doctor-signatures",
                UUID.randomUUID() + "_" + signatureFile.getOriginalFilename(),
                signatureFile.getContentType());

        userSettingService.updateDoctorFields(userId,
                licenseNumber,
                storageService.getUrl(path),
                null,
                null);

        log.info("Doctor onboarded: userId={}", userId);
    }

    // -------------------------------------------------------------------------
    // Super Admin actions
    // -------------------------------------------------------------------------

    public void approveDoctorApplication(Long applicationId) {
        DoctorApplication application = doctorApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NoSuchElementException("Doctor application not found"));

        if (application.getStatus() != DoctorApplicationStatus.PENDING) {
            throw new IllegalArgumentException("Application is not pending");
        }

        Role doctorRole = roleRepository.findByName(Roles.Doctor.name())
                .orElseThrow(() -> new IllegalStateException("Doctor role not found in database"));

        User user = userRepository.findByEmail(application.getEmail()).orElseGet(User::new);
        String invitationToken = generateToken(32);
        user.setFirstName(application.getFirstName());
        user.setLastName(application.getLastName());
        user.setName(fullName(application.getFirstName(), application.getLastName(), application.getEmail()));
        user.setUsername(application.getEmail());
        user.setEmail(application.getEmail());
        user.setPhone(application.getPhone());
        user.setBio(application.getBio());
        user.setAvatarUrl(application.getProfilePictureUrl());
        user.setProfilePictureOption("upload");
        user.setVerified(true);
        user.setOnboarded(true);
        user.setOnboardingStage(5);
        user.setMustChangePassword(true);
        user.setInvitationToken(invitationToken);
        user.setInvitationTokenExpiry(LocalDateTime.now().plusDays(7));
        user.setPassword(passwordEncoder.encode(invitationToken));
        user.setType("INDIVIDUAL");
        user.setCredits(user.getCredits() != null ? user.getCredits() : 0);
        user.setRole(doctorRole);
        userRepository.save(user);

        userSettingService.updateDoctorFields(user.getId(),
                application.getMedicalLicenseNumber(),
                application.getSignatureUrl(),
                application.getStampUrl(),
                DoctorApplicationStatus.APPROVED);

        application.setStatus(DoctorApplicationStatus.APPROVED);
        application.setReviewedAt(LocalDateTime.now());
        application.setCreatedUser(user);
        doctorApplicationRepository.save(application);

        queueService.dispatch(JobType.EMAIL_DOCTOR_APPLICATION_APPROVED, Map.of(
                "to", user.getEmail(),
                "subject", "Your TMAG Doctor Application Has Been Approved",
                "variables", Map.of(
                        "firstName", firstName(user),
                        "onboardingLink", frontendUrl + "/accept-invitation?token=" + invitationToken)));

        log.info("Doctor application approved: applicationId={} userId={}", applicationId, user.getId());
    }

    public void rejectDoctorApplication(Long applicationId, String reason) {
        DoctorApplication application = doctorApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NoSuchElementException("Doctor application not found"));

        if (application.getStatus() != DoctorApplicationStatus.PENDING) {
            throw new IllegalArgumentException("Application is not pending");
        }

        application.setStatus(DoctorApplicationStatus.REJECTED);
        application.setRejectionReason(reason);
        application.setReviewedAt(LocalDateTime.now());
        doctorApplicationRepository.save(application);

        queueService.dispatch(JobType.EMAIL_GENERIC, Map.of(
                "to", application.getEmail(),
                "subject", "Update on Your TMAG Doctor Application",
                "variables", Map.of(
                        "firstName", application.getFirstName() != null ? application.getFirstName() : "there",
                        "content",
                        "We regret to inform you that your doctor application was not approved. Reason: " + reason)));

        log.info("Doctor application rejected: applicationId={} reason={}", applicationId, reason);
    }

    public void inviteDoctor(String email, String firstName, String lastName) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setName(firstName + " " + lastName);
            user.setVerified(true);
        }

        Role doctorRole = roleRepository.findByName(Roles.Doctor.name())
                .orElseThrow(() -> new IllegalStateException("Doctor role not found in database"));

        user.setRole(doctorRole);
        userRepository.save(user);

        UserSetting settings = userSettingService.getOrCreateByUserId(user.getId());
        settings.setDoctorApplicationStatus(DoctorApplicationStatus.APPROVED);
        userSettingService.updateDoctorFields(user.getId(), null, null, null, DoctorApplicationStatus.APPROVED);

        queueService.dispatch(JobType.EMAIL_DOCTOR_INVITATION, Map.of(
                "to", email,
                "subject", "You Have Been Invited to Join TMAG as a Doctor",
                "variables", Map.of(
                        "firstName", firstName,
                        "onboardingLink", frontendUrl + "/doctor/onboarding")));

        log.info("Doctor invited: email={}", email);
    }

    // -------------------------------------------------------------------------
    // Dashboard stats
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public DoctorDashboardStats getDashboardStats(Long doctorId) {
        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime tomorrowStart = todayStart.plusDays(1);
        long pendingValidations = travelPlanRepository.countPendingDoctorValidation();
        long approvedToday = travelPlanRepository.countApprovedByDoctorBetween(doctorId, todayStart, tomorrowStart);
        long totalValidated = travelPlanRepository.countValidatedByDoctor(doctorId);

        List<DoctorValidationPlanDto> recentPlans = travelPlanRepository
                .findPendingDoctorValidationSummaries(PageRequest.of(0, 5))
                .map(this::toDoctorValidationPlanDto)
                .getContent();

        return new DoctorDashboardStats(pendingValidations, approvedToday, totalValidated, recentPlans);
    }

    // -------------------------------------------------------------------------
    // Plan lists
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<DoctorValidationPlanDto> getPendingPlansDto(Pageable pageable) {
        return travelPlanRepository.findPendingDoctorValidationSummaries(pageable)
                .map(this::toDoctorValidationPlanDto);
    }

    @Transactional(readOnly = true)
    public Page<DoctorValidationPlanDto> getPendingPlansDto(Long doctorId, Pageable pageable) {
        return travelPlanRepository.findPendingDoctorValidationSummariesForDoctor(doctorId, pageable)
                .map(this::toDoctorValidationPlanDto);
    }

    @Transactional(readOnly = true)
    public Page<DoctorValidationPlanDto> getValidatedPlansDto(Long doctorId, Pageable pageable) {
        return travelPlanRepository.findValidatedDoctorValidationSummaries(doctorId, pageable)
                .map(this::toDoctorValidationPlanDto);
    }

    @Transactional(readOnly = true)
    public List<DoctorPlanResponse> getPendingPlans() {
        return travelPlanRepository.findPendingDoctorValidation().stream()
                .map(this::toDoctorPlanResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DoctorPlanResponse> getApprovedPlans(Long doctorId) {
        return travelPlanRepository.findApprovedByDoctor(doctorId).stream()
                .map(this::toDoctorPlanResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DoctorPlanResponse> getRejectedPlans(Long doctorId) {
        return travelPlanRepository.findRejectedByDoctor(doctorId).stream()
                .map(this::toDoctorPlanResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DoctorValidationDetailDto getPlanDetailDto(Long planId) {
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new NoSuchElementException("Travel plan not found"));

        GeneratedPlan gp = generatedPlanRepository.findByTravelPlanId(planId).orElse(null);

        User traveller = plan.getUser();
        User validatedBy = plan.getValidatedBy();

        GeneratedPlanSnapshot snapshot = gp == null ? null
                : new GeneratedPlanSnapshot(
                        gp.getStatus(),
                        gp.getPlanJson(),
                        gp.getProvider(),
                        gp.getModelUsed(),
                        gp.getTokensUsed(),
                        gp.getProcessingTimeMs(),
                        gp.getErrorMessage(),
                        gp.getSignedPdfUrl(),
                        gp.getIsSigned());

        Object parsedContent = null;
        if (gp != null && gp.getPlanJson() != null) {
            try {
                parsedContent = objectMapper.readValue(gp.getPlanJson(), Object.class);
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse planJson for planId={}: {}", planId, e.getMessage());
            }
        }

        List<AssignedDoctorDto> assignedDoctors = assignmentRepository.findByTravelPlanIdAndDeletedAtIsNull(planId).stream()
                .map(a -> {
                    User d = a.getDoctor();
                    return new AssignedDoctorDto(
                            d.getId(),
                            d.getFirstName(),
                            d.getLastName(),
                            d.getEmail(),
                            avatarUrlService.toFullUrl(d.getAvatarUrl()));
                })
                .toList();
        Boolean openToAllDoctors = assignedDoctors.isEmpty() ? true : null;
        return new DoctorValidationDetailDto(
                plan.getId(),
                plan.getDestination(),
                plan.getCountry(),
                plan.getPurpose(),
                plan.getDuration(),
                plan.getRiskScore(),
                plan.getDoctorValidationStatus() != null ? plan.getDoctorValidationStatus().name() : "NOT_REQUIRED",
                plan.getValidatedAt(),
                validatedBy != null ? (validatedBy.getFirstName() + " " + validatedBy.getLastName()).trim() : null,
                plan.getRejectionReason(),
                plan.getPlanTier() != null ? plan.getPlanTier().name() : "FREE",
                traveller != null ? (traveller.getFirstName() + " " + traveller.getLastName()).trim() : "",
                traveller != null ? traveller.getEmail() : "",
                traveller != null ? traveller.getPhone() : "",
                plan.getCreatedAt(),
                snapshot,
                parsedContent,
                assignedDoctors,
                openToAllDoctors);
    }

    @Transactional(readOnly = true)
    public TravelPlan getPlanForReview(Long planId) {
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new NoSuchElementException("Travel plan not found"));
        if (plan.getDoctorValidationStatus() != DoctorValidationStatus.PENDING) {
            throw new IllegalArgumentException("Plan is not pending validation");
        }
        return plan;
    }

    // -------------------------------------------------------------------------
    // Unified validate (approve or reject)
    // -------------------------------------------------------------------------

    public void validatePlan(Long planId, Long doctorId, boolean approved, String rejectionReason) {
        if (approved) {
            approvePlan(planId, doctorId);
        } else {
            rejectPlan(planId, doctorId, rejectionReason != null ? rejectionReason : "No reason provided");
        }
    }

    public void approvePlan(Long planId, Long doctorId) {
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new NoSuchElementException("Travel plan not found"));
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new NoSuchElementException("Doctor not found"));

        if (plan.getDoctorValidationStatus() != DoctorValidationStatus.PENDING) {
            throw new IllegalArgumentException("Plan is not pending validation");
        }

        GeneratedPlan generated = generatedPlanRepository.findByTravelPlanId(planId)
                .orElseThrow(() -> new NoSuchElementException("Generated plan not found"));

        UserSetting doctorSettings = userSettingService.getOrCreateByUserId(doctorId);
        byte[] signedPdf = pdfGenerator.generateSignedPdf(plan, generated, doctor, doctorSettings);
        String filename = "signed-plan-" + planId + "-" + UUID.randomUUID() + ".pdf";
        String storagePath = storageService.storeBytes(signedPdf, "signed-plans", filename, "application/pdf");
        String signedPdfUrl = storageService.getUrl(storagePath);

        generated.setSignedPdfUrl(signedPdfUrl);
        generated.setIsSigned(true);
        generatedPlanRepository.save(generated);

        plan.setDoctorValidationStatus(DoctorValidationStatus.APPROVED);
        plan.setValidatedBy(doctor);
        plan.setValidatedAt(LocalDateTime.now());
        travelPlanRepository.save(plan);

        User traveller = plan.getUser();
        if (traveller != null && traveller.getEmail() != null) {
            String html = emailTemplates.planApprovedEmail(firstName(traveller), plan.getDestination());
            emailService.sendEmailWithAttachment(
                    traveller.getEmail(),
                    "Your Travel Health Plan for " + plan.getDestination() + " Has Been Approved",
                    html,
                    signedPdf,
                    "travel-health-plan-" + plan.getDestination().replaceAll("[^a-zA-Z0-9]", "-") + ".pdf",
                    "application/pdf");
        }

        log.info("Plan approved and signed: planId={} doctorId={}", planId, doctorId);
    }

    public void rejectPlan(Long planId, Long doctorId, String reason) {
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new NoSuchElementException("Travel plan not found"));
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new NoSuchElementException("Doctor not found"));

        if (plan.getDoctorValidationStatus() != DoctorValidationStatus.PENDING) {
            throw new IllegalArgumentException("Plan is not pending validation");
        }

        plan.setDoctorValidationStatus(DoctorValidationStatus.ELEVATED);
        plan.setValidatedBy(doctor);
        plan.setValidatedAt(LocalDateTime.now());
        plan.setRejectionReason(reason);
        travelPlanRepository.save(plan);

        User traveller = plan.getUser();
        if (traveller != null && traveller.getEmail() != null) {
            String html = emailTemplates.planEscalatedEmail(firstName(traveller), plan.getDestination());
            emailService.sendHtmlEmail(
                    traveller.getEmail(),
                    "Your Travel Health Plan Has Been Escalated for Review",
                    html);
        }

        // Notify super admins
        notifySuperAdminsOfEscalatedPlan(plan, reason, doctor);

        log.info("Plan escalated for review: planId={} doctorId={} reason={}", planId, doctorId, reason);
    }

    // -------------------------------------------------------------------------
    // Signed PDF download
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public byte[] getSignedPdf(Long planId, Long doctorId) {
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new NoSuchElementException("Travel plan not found"));

        if (plan.getDoctorValidationStatus() != DoctorValidationStatus.APPROVED) {
            throw new IllegalArgumentException("Plan has not been approved");
        }

        GeneratedPlan gp = generatedPlanRepository.findByTravelPlanId(planId)
                .orElseThrow(() -> new NoSuchElementException("Generated plan not found"));

        if (gp.getSignedPdfUrl() == null) {
            throw new IllegalStateException("Signed PDF not available");
        }

        return storageService.readBytes(gp.getSignedPdfUrl());
    }

    // -------------------------------------------------------------------------
    // Profile
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public DoctorProfileResponse getDoctorProfile(Long doctorId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new NoSuchElementException("Doctor not found"));
        UserSetting settings = userSettingService.getOrCreateByUserId(doctorId);
        return new DoctorProfileResponse(
                doctor.getId(),
                doctor.getFirstName(),
                doctor.getLastName(),
                doctor.getEmail(),
                doctor.getPhone(),
                avatarUrlService.toFullUrl(doctor.getAvatarUrl()),
                doctor.getProfilePictureOption(),
                doctor.getBio(),
                settings.getMedicalLicenseNumber(),
                settings.getSignatureUrl(),
                settings.getStampUrl(),
                settings.getDoctorApplicationStatus() != null ? settings.getDoctorApplicationStatus().name() : null,
                doctor.getCreatedAt());
    }

    public void revokeDoctor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Role individualRole = roleRepository.findByName(Roles.Individual.name())
                .orElseThrow(() -> new IllegalStateException("Individual role not found in database"));

        user.setRole(individualRole);
        userRepository.save(user);

        userSettingService.updateDoctorFields(userId, null, null, null, DoctorApplicationStatus.NONE);

        log.info("Doctor privileges revoked: userId={}", userId);
    }

    @Transactional(readOnly = true)
    public List<DoctorReviewerDto> getApprovedReviewers() {
        return userRepository.findByRoleName(Roles.Doctor.name()).stream()
                .map(doctor -> new DoctorReviewerDto(
                        doctor.getId(),
                        doctor.getFirstName(),
                        doctor.getLastName(),
                        doctor.getEmail(),
                        avatarUrlService.toFullUrl(doctor.getAvatarUrl()),
                        doctor.getBio()))
                .toList();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private DoctorValidationPlanDto toDoctorValidationPlanDto(DoctorValidationPlanProjection plan) {
        List<AssignedDoctorDto> assignedDoctors = assignmentRepository.findByTravelPlanIdAndDeletedAtIsNull(plan.getPlanId()).stream()
                .map(a -> {
                    User d = a.getDoctor();
                    return new AssignedDoctorDto(
                            d.getId(),
                            d.getFirstName(),
                            d.getLastName(),
                            d.getEmail(),
                            avatarUrlService.toFullUrl(d.getAvatarUrl()));
                })
                .toList();
        Boolean openToAllDoctors = assignedDoctors.isEmpty() ? true : null;
        return new DoctorValidationPlanDto(
                plan.getPlanId(),
                plan.getDestination(),
                plan.getCountry(),
                plan.getPurpose(),
                plan.getDuration(),
                plan.getRiskScore(),
                plan.getValidationStatus() != null ? plan.getValidationStatus().name() : "NOT_REQUIRED",
                plan.getPlanTier() != null ? plan.getPlanTier().name() : "FREE",
                fullName(plan.getTravellerFirstName(), plan.getTravellerLastName(), plan.getTravellerName()),
                plan.getTravellerEmail() != null ? plan.getTravellerEmail() : "",
                plan.getCreatedAt(),
                plan.getGeneratedPlanStatus(),
                assignedDoctors,
                openToAllDoctors);
    }

    private DoctorPlanResponse toDoctorPlanResponse(TravelPlan plan) {
        User user = plan.getUser();
        return new DoctorPlanResponse(
                plan.getId(),
                plan.getDestination(),
                plan.getCountry(),
                user != null ? (user.getFirstName() + " " + user.getLastName()).trim() : "",
                user != null ? user.getEmail() : "",
                plan.getCreatedAt(),
                plan.getPlanTier() != null ? plan.getPlanTier().name() : "FREE",
                plan.getStatus(),
                plan.getDoctorValidationStatus() != null ? plan.getDoctorValidationStatus().name() : "NOT_REQUIRED");
    }

    private String firstName(User user) {
        if (user.getFirstName() != null && !user.getFirstName().isBlank()) {
            return user.getFirstName();
        }
        return "there";
    }

    private String fullName(String firstName, String lastName, String fallbackName) {
        String first = firstName != null ? firstName : "";
        String last = lastName != null ? lastName : "";
        String name = (first + " " + last).trim();
        return !name.isBlank() ? name : (fallbackName != null ? fallbackName : "");
    }

    private String generateToken(int byteLength) {
        byte[] bytes = new byte[byteLength];
        new java.security.SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private byte[] readMultipartFile(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }
    }

    private void notifySuperAdminsOfEscalatedPlan(TravelPlan plan, String reason, User doctor) {
        try {
            // Get all super admins to notify
            Role superAdminRole = roleRepository.findByName(Roles.SuperAdmin.name()).orElse(null);
            if (superAdminRole != null) {
                List<User> superAdmins = userRepository.findByRole(superAdminRole.getId());

                String travellerName = plan.getUser() != null
                        ? (plan.getUser().getFirstName() + " " + plan.getUser().getLastName()).trim()
                        : "Unknown";
                String doctorName = doctor != null ? (doctor.getFirstName() + " " + doctor.getLastName()).trim()
                        : "Unknown";
                String feedbackMessage = "Doctor: " + doctorName + "\nFeedback: " + reason;

                for (User superAdmin : superAdmins) {
                    if (superAdmin.getEmail() != null) {
                        String html = emailTemplates.planEscalatedNotificationEmail(travellerName, plan.getDestination(),
                                feedbackMessage);
                        emailService.sendHtmlEmail(
                                superAdmin.getEmail(),
                                "Escalated Plan Review Required - " + travellerName + " for " + plan.getDestination(),
                                html);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to notify super admins of escalated plan: {}", e.getMessage());
        }
    }
}
