package com.TravelMedicineAdvisory.Server.domain.doctor;

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
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlan;
import com.TravelMedicineAdvisory.Server.domain.travelplan.DoctorValidationPlanProjection;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanPdfGenerator;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.TravelMedicineAdvisory.Server.domain.usersetting.UserSetting;
import com.TravelMedicineAdvisory.Server.domain.usersetting.UserSettingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

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
            UserSettingService userSettingService) {
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
    }

    // -------------------------------------------------------------------------
    // Doctor onboarding / application
    // -------------------------------------------------------------------------

    public void applyToBecomeDoctor(Long userId, String licenseNumber, MultipartFile signatureFile,
            MultipartFile stampFile) {
        // User user = userRepository.findById(userId)
        // .orElseThrow(() -> new NoSuchElementException("User not found"));

        UserSetting settings = userSettingService.getOrCreateByUserId(userId);

        if (settings.getDoctorApplicationStatus() != null
                && settings.getDoctorApplicationStatus() != DoctorApplicationStatus.NONE
                && settings.getDoctorApplicationStatus() != DoctorApplicationStatus.REJECTED) {
            throw new IllegalArgumentException("You have already applied or are already a doctor");
        }

        String sigPath = storageService.storeBytes(
                readMultipartFile(signatureFile),
                "doctor-signatures",
                UUID.randomUUID() + "_" + signatureFile.getOriginalFilename(),
                signatureFile.getContentType());

        settings.setMedicalLicenseNumber(licenseNumber);
        settings.setSignatureUrl(storageService.getUrl(sigPath));

        if (stampFile != null && !stampFile.isEmpty()) {
            String stampPath = storageService.storeBytes(
                    readMultipartFile(stampFile),
                    "doctor-stamps",
                    UUID.randomUUID() + "_" + stampFile.getOriginalFilename(),
                    stampFile.getContentType());
            settings.setStampUrl(storageService.getUrl(stampPath));
        }

        settings.setDoctorApplicationStatus(DoctorApplicationStatus.PENDING);
        userSettingService.updateDoctorFields(userId,
                settings.getMedicalLicenseNumber(),
                settings.getSignatureUrl(),
                settings.getStampUrl(),
                DoctorApplicationStatus.PENDING);

        log.info("Doctor application submitted: userId={}", userId);
    }

    public DoctorProfileResponse updateDoctorProfile(Long userId, String firstName, String lastName,
            String licenseNumber, MultipartFile signatureFile, MultipartFile stampFile) {
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
        if (firstName != null || lastName != null) {
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

    public void approveDoctorApplication(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        UserSetting settings = userSettingService.getOrCreateByUserId(userId);

        if (settings.getDoctorApplicationStatus() != DoctorApplicationStatus.PENDING) {
            throw new IllegalArgumentException("User does not have a pending doctor application");
        }

        Role doctorRole = roleRepository.findByName(Roles.Doctor.name())
                .orElseThrow(() -> new IllegalStateException("Doctor role not found in database"));

        settings.setDoctorApplicationStatus(DoctorApplicationStatus.APPROVED);
        userSettingService.updateDoctorFields(userId, null, null, null, DoctorApplicationStatus.APPROVED);

        user.setRole(doctorRole);
        userRepository.save(user);

        queueService.dispatch(JobType.EMAIL_DOCTOR_APPLICATION_APPROVED, Map.of(
                "to", user.getEmail(),
                "subject", "Your TMAG Doctor Application Has Been Approved",
                "variables", Map.of(
                        "firstName", firstName(user),
                        "onboardingLink", frontendUrl + "/doctor/onboarding")));

        log.info("Doctor application approved: userId={}", userId);
    }

    public void rejectDoctorApplication(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        UserSetting settings = userSettingService.getOrCreateByUserId(userId);

        if (settings.getDoctorApplicationStatus() != DoctorApplicationStatus.PENDING) {
            throw new IllegalArgumentException("User does not have a pending doctor application");
        }

        userSettingService.updateDoctorFields(userId, null, null, null, DoctorApplicationStatus.REJECTED);

        queueService.dispatch(JobType.EMAIL_GENERIC, Map.of(
                "to", user.getEmail(),
                "subject", "Update on Your TMAG Doctor Application",
                "variables", Map.of(
                        "firstName", firstName(user),
                        "content",
                        "We regret to inform you that your doctor application was not approved. Reason: " + reason)));

        log.info("Doctor application rejected: userId={} reason={}", userId, reason);
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
                parsedContent);
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
            String html = emailTemplates.planElevatedEmail(firstName(traveller), plan.getDestination());
            emailService.sendHtmlEmail(
                    traveller.getEmail(),
                    "Your Travel Health Plan Has Been Elevated for Review",
                    html);
        }

        // Notify super admins
        notifySuperAdminsOfElevatedPlan(plan, reason, doctor);

        log.info("Plan elevated for review: planId={} doctorId={} reason={}", planId, doctorId, reason);
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

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private DoctorValidationPlanDto toDoctorValidationPlanDto(DoctorValidationPlanProjection plan) {
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
                plan.getGeneratedPlanStatus());
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

    private byte[] readMultipartFile(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }
    }

    private void notifySuperAdminsOfElevatedPlan(TravelPlan plan, String reason, User doctor) {
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
                        String html = emailTemplates.planElevatedNotificationEmail(travellerName, plan.getDestination(),
                                feedbackMessage);
                        emailService.sendHtmlEmail(
                                superAdmin.getEmail(),
                                "Elevated Plan Review Required - " + travellerName + " for " + plan.getDestination(),
                                html);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to notify super admins of elevated plan: {}", e.getMessage());
        }
    }
}
