package com.TravelMedicineAdvisory.Server.domain.admin.plans;

import com.TravelMedicineAdvisory.Server.core.email.EmailService;
import com.TravelMedicineAdvisory.Server.core.email.EmailTemplates;
import com.TravelMedicineAdvisory.Server.core.storage.StorageService;
import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorValidationStatus;
import com.TravelMedicineAdvisory.Server.domain.plans.GeneratedPlan;
import com.TravelMedicineAdvisory.Server.domain.plans.GeneratedPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlan;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanPdfGenerator;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.usersetting.UserSetting;
import com.TravelMedicineAdvisory.Server.domain.usersetting.UserSettingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class SuperAdminPlanService {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminPlanService.class);

    private final TravelPlanRepository travelPlanRepository;
    private final GeneratedPlanRepository generatedPlanRepository;
    private final TravelPlanPdfGenerator pdfGenerator;
    private final StorageService storageService;
    private final EmailService emailService;
    private final EmailTemplates emailTemplates;
    private final UserSettingService userSettingService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public SuperAdminPlanService(
            TravelPlanRepository travelPlanRepository,
            GeneratedPlanRepository generatedPlanRepository,
            TravelPlanPdfGenerator pdfGenerator,
            StorageService storageService,
            EmailService emailService,
            EmailTemplates emailTemplates,
            UserSettingService userSettingService) {
        this.travelPlanRepository = travelPlanRepository;
        this.generatedPlanRepository = generatedPlanRepository;
        this.pdfGenerator = pdfGenerator;
        this.storageService = storageService;
        this.emailService = emailService;
        this.emailTemplates = emailTemplates;
        this.userSettingService = userSettingService;
    }

    /**
     * Approve an elevated plan (generate signed PDF and send to user)
     */
    public void approveElevatedPlan(Long planId) {
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new NoSuchElementException("Travel plan not found"));

        if (plan.getDoctorValidationStatus() != DoctorValidationStatus.ELEVATED) {
            throw new IllegalArgumentException("Plan is not in elevated status");
        }

        GeneratedPlan generated = generatedPlanRepository.findByTravelPlanId(planId)
                .orElseThrow(() -> new NoSuchElementException("Generated plan not found"));

        // Create a system user for the super admin approval
        // Use the validating doctor info (who initially rejected) as context
        User doctor = plan.getValidatedBy();
        if (doctor == null) {
            throw new IllegalArgumentException("Plan does not have a validating doctor reference");
        }

        UserSetting doctorSettings = userSettingService.getOrCreateByUserId(doctor.getId());
        byte[] signedPdf = pdfGenerator.generateSignedPdf(plan, generated, doctor, doctorSettings);
        String filename = "signed-plan-" + planId + "-" + UUID.randomUUID() + ".pdf";
        String storagePath = storageService.storeBytes(signedPdf, "signed-plans", filename, "application/pdf");
        String signedPdfUrl = storageService.getUrl(storagePath);

        generated.setSignedPdfUrl(signedPdfUrl);
        generated.setIsSigned(true);
        generatedPlanRepository.save(generated);

        plan.setDoctorValidationStatus(DoctorValidationStatus.APPROVED);
        plan.setValidatedAt(LocalDateTime.now());
        travelPlanRepository.save(plan);

        User recipient = notificationUser(plan, generated);
        String recipientEmail = notificationEmail(plan, generated);
        if (recipientEmail != null) {
            String html = emailTemplates.planApprovedEmail(firstName(recipient), plan.getDestination());
            emailService.sendEmailWithAttachment(
                    recipientEmail,
                    "Your Travel Health Plan for " + plan.getDestination() + " Has Been Approved",
                    html,
                    signedPdf,
                    "travel-health-plan-" + plan.getDestination().replaceAll("[^a-zA-Z0-9]", "-") + ".pdf",
                    "application/pdf");
        }

        log.info("Elevated plan approved by super admin: planId={}", planId);
    }

    /**
     * Reject an elevated plan (send rejection to user)
     */
    public void rejectElevatedPlan(Long planId, String reason) {
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new NoSuchElementException("Travel plan not found"));

        if (plan.getDoctorValidationStatus() != DoctorValidationStatus.ELEVATED) {
            throw new IllegalArgumentException("Plan is not in elevated status");
        }

        plan.setDoctorValidationStatus(DoctorValidationStatus.REJECTED);
        plan.setValidatedAt(LocalDateTime.now());
        plan.setRejectionReason(reason);
        travelPlanRepository.save(plan);

        GeneratedPlan generated = generatedPlanRepository.findByTravelPlanId(planId).orElse(null);
        User recipient = notificationUser(plan, generated);
        String recipientEmail = notificationEmail(plan, generated);
        if (recipientEmail != null) {
            String html = emailTemplates.planRejectedEmail(firstName(recipient), plan.getDestination(), reason);
            emailService.sendHtmlEmail(
                    recipientEmail,
                    "Update on Your Travel Health Plan for " + plan.getDestination(),
                    html);
        }

        log.info("Elevated plan rejected by super admin: planId={} reason={}", planId, reason);
    }

    @Transactional(readOnly = true)
    public byte[] previewPlanPdf(Long planId) {
        TravelPlan plan = findElevatedPlan(planId);
        GeneratedPlan generated = generatedPlanRepository.findByTravelPlanId(planId).orElse(null);
        return pdfGenerator.generate(plan, generated);
    }

    @Transactional(readOnly = true)
    public byte[] previewSummaryPdf(Long planId) {
        findElevatedPlan(planId);
        GeneratedPlan generated = generatedPlanRepository.findByTravelPlanId(planId)
                .orElseThrow(() -> new NoSuchElementException("Generated plan not found"));
        if (generated.getSummaryPdfUrl() == null || generated.getSummaryPdfUrl().isBlank()) {
            throw new IllegalArgumentException("Stored summary PDF is not available for this plan");
        }
        return storageService.readBytes(generated.getSummaryPdfUrl());
    }

    private TravelPlan findElevatedPlan(Long planId) {
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new NoSuchElementException("Travel plan not found"));
        if (plan.getDoctorValidationStatus() != DoctorValidationStatus.ELEVATED) {
            throw new IllegalArgumentException("Plan is not in elevated status");
        }
        return plan;
    }

    private User notificationUser(TravelPlan plan, GeneratedPlan generated) {
        if (plan.getUser() != null) {
            return plan.getUser();
        }
        if (generated != null && generated.getUser() != null) {
            return generated.getUser();
        }
        return null;
    }

    private String notificationEmail(TravelPlan plan, GeneratedPlan generated) {
        if (plan.getUser() != null && plan.getUser().getEmail() != null) {
            return plan.getUser().getEmail();
        }
        if (plan.getEmployee() != null && plan.getEmployee().getEmail() != null) {
            return plan.getEmployee().getEmail();
        }
        if (generated != null && generated.getUser() != null) {
            return generated.getUser().getEmail();
        }
        return null;
    }

    private String firstName(User user) {
        if (user == null) {
            return "there";
        }
        if (user.getFirstName() != null && !user.getFirstName().isBlank()) {
            return user.getFirstName();
        }
        return "there";
    }
}
