package com.TravelMedicineAdvisory.Server.domain.admin.plans;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorValidationStatus;
import com.TravelMedicineAdvisory.Server.domain.plans.GeneratedPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.travelplan.ElevatedPlanProjection;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/admin/plans")
@PreAuthorize("hasRole('SUPERADMIN')")
public class SuperAdminPlanController {

    private final TravelPlanRepository travelPlanRepository;
    private final GeneratedPlanRepository generatedPlanRepository;
    private final SuperAdminPlanService superAdminPlanService;

    public SuperAdminPlanController(
            TravelPlanRepository travelPlanRepository,
            GeneratedPlanRepository generatedPlanRepository,
            SuperAdminPlanService superAdminPlanService) {
        this.travelPlanRepository = travelPlanRepository;
        this.generatedPlanRepository = generatedPlanRepository;
        this.superAdminPlanService = superAdminPlanService;
    }

    /**
     * Get all elevated plans
     */
    @GetMapping("/elevated")
    @Transactional(readOnly = true)
    public ResponseEntity<SuccessResponse> getElevatedPlans(Pageable pageable) {
        Page<ElevatedPlanResponse> elevatedPlans = travelPlanRepository
                .findElevatedPlanSummaries(DoctorValidationStatus.ELEVATED, pageable)
                .map(this::toElevatedPlanResponse);
        return ResponseEntity.ok(new SuccessResponse("Elevated plans retrieved successfully", elevatedPlans));
    }

    /**
     * Approve an elevated plan (final decision by super admin)
     */
    @PostMapping("/{planId}/approve")
    public ResponseEntity<SuccessResponse> approveElevatedPlan(@PathVariable Long planId) {
        try {
            superAdminPlanService.approveElevatedPlan(planId);
            return ResponseEntity.ok(new SuccessResponse("Plan approved successfully", null));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(new SuccessResponse("Plan not found", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new SuccessResponse(e.getMessage(), null));
        }
    }

    /**
     * Reject an elevated plan (final decision by super admin)
     */
    @PostMapping("/{planId}/reject")
    public ResponseEntity<SuccessResponse> rejectElevatedPlan(
            @PathVariable Long planId,
            @RequestBody ElevatedPlanDecisionRequest request) {
        try {
            superAdminPlanService.rejectElevatedPlan(planId, request.reason());
            return ResponseEntity.ok(new SuccessResponse("Plan rejected successfully", null));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(new SuccessResponse("Plan not found", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new SuccessResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/{planId}/preview-pdf")
    public ResponseEntity<byte[]> previewElevatedPlanPdf(@PathVariable Long planId) {
        byte[] pdf = superAdminPlanService.previewPlanPdf(planId);
        return pdfResponse(pdf, "elevated-plan-" + planId + ".pdf");
    }

    @GetMapping("/{planId}/preview-summary")
    public ResponseEntity<byte[]> previewElevatedPlanSummary(@PathVariable Long planId) {
        byte[] pdf = superAdminPlanService.previewSummaryPdf(planId);
        return pdfResponse(pdf, "elevated-plan-" + planId + "-summary.pdf");
    }

    public record ElevatedPlanDecisionRequest(String reason) {}

    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String filename) {
        ContentDisposition disposition = ContentDisposition.inline()
                .filename(filename, StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private ElevatedPlanResponse toElevatedPlanResponse(ElevatedPlanProjection plan) {
        return new ElevatedPlanResponse(
                plan.getId(),
                plan.getDestination(),
                plan.getDuration(),
                plan.getPurpose(),
                plan.getRiskScore(),
                fullName(plan.getTravellerFirstName(), plan.getTravellerLastName(), plan.getTravellerName()),
                plan.getTravellerEmail() != null ? plan.getTravellerEmail() : "",
                fullName(plan.getDoctorFirstName(), plan.getDoctorLastName(), plan.getDoctorName()),
                plan.getDoctorFeedback(),
                plan.getPdfPreviewUrl(),
                plan.getSummaryPreviewUrl(),
                plan.getElevatedAt());
    }

    private String fullName(String firstNameValue, String lastNameValue, String fallbackName) {
        String firstName = firstNameValue != null ? firstNameValue : "";
        String lastName = lastNameValue != null ? lastNameValue : "";
        String name = (firstName + " " + lastName).trim();
        return !name.isBlank() ? name : (fallbackName != null ? fallbackName : "");
    }
}
