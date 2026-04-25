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

        // Notify traveller
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

        // Notify traveller
        User traveller = plan.getUser();
        if (traveller != null && traveller.getEmail() != null) {
            String html = emailTemplates.planRejectedEmail(firstName(traveller), plan.getDestination(), reason);
            emailService.sendHtmlEmail(
                    traveller.getEmail(),
                    "Update on Your Travel Health Plan for " + plan.getDestination(),
                    html);
        }

        log.info("Elevated plan rejected by super admin: planId={} reason={}", planId, reason);
    }

    private String firstName(User user) {
        if (user.getFirstName() != null && !user.getFirstName().isBlank()) {
            return user.getFirstName();
        }
        return "there";
    }
}
