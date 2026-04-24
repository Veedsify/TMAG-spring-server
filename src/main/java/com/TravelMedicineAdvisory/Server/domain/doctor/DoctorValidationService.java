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
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanPdfGenerator;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
            ObjectMapper objectMapper) {
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
    }

    // -------------------------------------------------------------------------
    // Doctor onboarding / application
    // -------------------------------------------------------------------------

    public void applyToBecomeDoctor(Long userId, String licenseNumber, MultipartFile signatureFile, MultipartFile stampFile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (user.getDoctorApplicationStatus() != null
                && user.getDoctorApplicationStatus() != DoctorApplicationStatus.NONE
                && user.getDoctorApplicationStatus() != DoctorApplicationStatus.REJECTED) {
            throw new IllegalArgumentException("You have already applied or are already a doctor");
        }

        String sigPath = storageService.storeBytes(
                readMultipartFile(signatureFile),
                "doctor-signatures",
                UUID.randomUUID() + "_" + signatureFile.getOriginalFilename(),
                signatureFile.getContentType());

        user.setMedicalLicenseNumber(licenseNumber);
        user.setSignatureUrl(storageService.getUrl(sigPath));

        if (stampFile != null && !stampFile.isEmpty()) {
            String stampPath = storageService.storeBytes(
                    readMultipartFile(stampFile),
                    "doctor-stamps",
                    UUID.randomUUID() + "_" + stampFile.getOriginalFilename(),
                    stampFile.getContentType());
            user.setStampUrl(storageService.getUrl(stampPath));
        }

        user.setDoctorApplicationStatus(DoctorApplicationStatus.PENDING);
        userRepository.save(user);

        log.info("Doctor application submitted: userId={}", userId);
    }

    public DoctorProfileResponse updateDoctorProfile(Long userId, String firstName, String lastName,
            String licenseNumber, MultipartFile signatureFile, MultipartFile stampFile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Doctor not found"));

        if (user.getDoctorApplicationStatus() != DoctorApplicationStatus.APPROVED) {
            throw new IllegalArgumentException("Only approved doctors can update their profile");
        }

        if (firstName != null && !firstName.isBlank()) user.setFirstName(firstName);
        if (lastName != null && !lastName.isBlank()) user.setLastName(lastName);
        if (licenseNumber != null && !licenseNumber.isBlank()) user.setMedicalLicenseNumber(licenseNumber);

        if (signatureFile != null && !signatureFile.isEmpty()) {
            String path = storageService.storeBytes(
                    readMultipartFile(signatureFile),
                    "doctor-signatures",
                    UUID.randomUUID() + "_" + signatureFile.getOriginalFilename(),
                    signatureFile.getContentType());
            user.setSignatureUrl(storageService.getUrl(path));
        }

        if (stampFile != null && !stampFile.isEmpty()) {
            String path = storageService.storeBytes(
                    readMultipartFile(stampFile),
                    "doctor-stamps",
                    UUID.randomUUID() + "_" + stampFile.getOriginalFilename(),
                    stampFile.getContentType());
            user.setStampUrl(storageService.getUrl(path));
        }

        userRepository.save(user);
        log.info("Doctor profile updated: userId={}", userId);
        return getDoctorProfile(userId);
    }

    public void onboardDoctor(Long userId, String licenseNumber, MultipartFile signatureFile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (user.getDoctorApplicationStatus() != DoctorApplicationStatus.APPROVED) {
            throw new IllegalArgumentException("You must be invited/approved before onboarding");
        }

        String path = storageService.storeBytes(
                readMultipartFile(signatureFile),
                "doctor-signatures",
                UUID.randomUUID() + "_" + signatureFile.getOriginalFilename(),
                signatureFile.getContentType());

        user.setMedicalLicenseNumber(licenseNumber);
        user.setSignatureUrl(storageService.getUrl(path));
        userRepository.save(user);

        log.info("Doctor onboarded: userId={}", userId);
    }

    // -------------------------------------------------------------------------
    // Super Admin actions
    // -------------------------------------------------------------------------

    public void approveDoctorApplication(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (user.getDoctorApplicationStatus() != DoctorApplicationStatus.PENDING) {
            throw new IllegalArgumentException("User does not have a pending doctor application");
        }

        Role doctorRole = roleRepository.findByName(Roles.Doctor.name())
                .orElseThrow(() -> new IllegalStateException("Doctor role not found in database"));

        user.setDoctorApplicationStatus(DoctorApplicationStatus.APPROVED);
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

        if (user.getDoctorApplicationStatus() != DoctorApplicationStatus.PENDING) {
            throw new IllegalArgumentException("User does not have a pending doctor application");
        }

        user.setDoctorApplicationStatus(DoctorApplicationStatus.REJECTED);
        userRepository.save(user);

        queueService.dispatch(JobType.EMAIL_GENERIC, Map.of(
                "to", user.getEmail(),
                "subject", "Update on Your TMAG Doctor Application",
                "variables", Map.of(
                        "firstName", firstName(user),
                        "content", "We regret to inform you that your doctor application was not approved. Reason: " + reason)));

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

        user.setDoctorApplicationStatus(DoctorApplicationStatus.APPROVED);
        user.setRole(doctorRole);
        userRepository.save(user);

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
        long pendingValidations = travelPlanRepository.findPendingDoctorValidation().size();
        long approvedToday = travelPlanRepository.countApprovedByDoctorToday(doctorId);
        long totalValidated = travelPlanRepository.countValidatedByDoctor(doctorId);

        List<TravelPlan> recentPending = travelPlanRepository.findPendingDoctorValidation();
        List<DoctorValidationPlanDto> recentPlans = recentPending.stream()
                .limit(5)
                .map(plan -> {
                    GeneratedPlan gp = generatedPlanRepository.findByTravelPlanId(plan.getId()).orElse(null);
                    return toDoctorValidationPlanDto(plan, gp);
                })
                .toList();

        return new DoctorDashboardStats(pendingValidations, approvedToday, totalValidated, recentPlans);
    }

    // -------------------------------------------------------------------------
    // Plan lists
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<DoctorValidationPlanDto> getPendingPlansDto() {
        return travelPlanRepository.findPendingDoctorValidation().stream()
                .map(plan -> {
                    GeneratedPlan gp = generatedPlanRepository.findByTravelPlanId(plan.getId()).orElse(null);
                    return toDoctorValidationPlanDto(plan, gp);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DoctorValidationPlanDto> getValidatedPlansDto(Long doctorId) {
        return travelPlanRepository.findValidatedByDoctor(doctorId).stream()
                .map(plan -> {
                    GeneratedPlan gp = generatedPlanRepository.findByTravelPlanId(plan.getId()).orElse(null);
                    return toDoctorValidationPlanDto(plan, gp);
                })
                .toList();
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

        GeneratedPlanSnapshot snapshot = gp == null ? null : new GeneratedPlanSnapshot(
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
            } catch (Exception e) {
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

        byte[] signedPdf = pdfGenerator.generateSignedPdf(plan, generated, doctor);
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

        plan.setDoctorValidationStatus(DoctorValidationStatus.REJECTED);
        plan.setValidatedBy(doctor);
        plan.setValidatedAt(LocalDateTime.now());
        plan.setRejectionReason(reason);
        travelPlanRepository.save(plan);

        User traveller = plan.getUser();
        if (traveller != null && traveller.getEmail() != null) {
            String html = emailTemplates.planRejectedEmail(firstName(traveller), plan.getDestination(), reason);
            emailService.sendHtmlEmail(
                    traveller.getEmail(),
                    "Update on Your Travel Health Plan for " + plan.getDestination(),
                    html);
        }

        log.info("Plan rejected: planId={} doctorId={} reason={}", planId, doctorId, reason);
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
        return new DoctorProfileResponse(
                doctor.getId(),
                doctor.getFirstName(),
                doctor.getLastName(),
                doctor.getEmail(),
                doctor.getPhone(),
                doctor.getMedicalLicenseNumber(),
                doctor.getSignatureUrl(),
                doctor.getStampUrl(),
                doctor.getDoctorApplicationStatus() != null ? doctor.getDoctorApplicationStatus().name() : null,
                doctor.getCreatedAt());
    }

    public void revokeDoctor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Role individualRole = roleRepository.findByName(Roles.Individual.name())
                .orElseThrow(() -> new IllegalStateException("Individual role not found in database"));

        user.setRole(individualRole);
        user.setDoctorApplicationStatus(DoctorApplicationStatus.NONE);
        userRepository.save(user);

        log.info("Doctor privileges revoked: userId={}", userId);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private DoctorValidationPlanDto toDoctorValidationPlanDto(TravelPlan plan, GeneratedPlan gp) {
        User user = plan.getUser();
        return new DoctorValidationPlanDto(
                plan.getId(),
                plan.getDestination(),
                plan.getCountry(),
                plan.getPurpose(),
                plan.getDuration(),
                plan.getRiskScore(),
                plan.getDoctorValidationStatus() != null ? plan.getDoctorValidationStatus().name() : "NOT_REQUIRED",
                plan.getPlanTier() != null ? plan.getPlanTier().name() : "FREE",
                user != null ? (user.getFirstName() + " " + user.getLastName()).trim() : "",
                user != null ? user.getEmail() : "",
                plan.getCreatedAt(),
                gp != null ? gp.getStatus() : null);
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

    private byte[] readMultipartFile(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }
    }
}
