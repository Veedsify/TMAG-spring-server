package com.TravelMedicineAdvisory.Server.domain.admin.plans;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.doctor.AssignedDoctorDto;
import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorValidationStatus;
import com.TravelMedicineAdvisory.Server.domain.doctor.TravelPlanDoctorAssignmentRepository;
import com.TravelMedicineAdvisory.Server.domain.plans.GeneratedPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.travelplan.EscalatedPlanProjection;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;

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
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/admin/plans")
@PreAuthorize("@perm.has(authentication, 'travel_plan:read', 'travel_plan:update')")
public class SuperAdminPlanController {

    private final TravelPlanRepository travelPlanRepository;
    private final GeneratedPlanRepository generatedPlanRepository;
    private final SuperAdminPlanService superAdminPlanService;
    private final TravelPlanDoctorAssignmentRepository assignmentRepository;

    public SuperAdminPlanController(
            TravelPlanRepository travelPlanRepository,
            GeneratedPlanRepository generatedPlanRepository,
            SuperAdminPlanService superAdminPlanService,
            TravelPlanDoctorAssignmentRepository assignmentRepository) {
        this.travelPlanRepository = travelPlanRepository;
        this.generatedPlanRepository = generatedPlanRepository;
        this.superAdminPlanService = superAdminPlanService;
        this.assignmentRepository = assignmentRepository;
    }

    /**
     * Get all escalated plans
     */
    @GetMapping("/escalated")
    @Transactional(readOnly = true)
    public ResponseEntity<SuccessResponse> getEscalatedPlans(Pageable pageable) {
        Page<EscalatedPlanResponse> escalatedPlans = travelPlanRepository
                .findEscalatedPlanSummaries(DoctorValidationStatus.ELEVATED, pageable)
                .map(this::toEscalatedPlanResponse);
        return ResponseEntity.ok(new SuccessResponse("Escalated plans retrieved successfully", escalatedPlans));
    }

    /**
     * Approve an escalated plan (final decision by super admin)
     */
    @PostMapping("/{planId}/approve")
    public ResponseEntity<SuccessResponse> approveEscalatedPlan(@PathVariable Long planId) {
        try {
            superAdminPlanService.approveEscalatedPlan(planId);
            return ResponseEntity.ok(new SuccessResponse("Plan approved successfully", null));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(new SuccessResponse("Plan not found", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new SuccessResponse(e.getMessage(), null));
        }
    }

    /**
     * Reject an escalated plan (final decision by super admin)
     */
    @PostMapping("/{planId}/reject")
    public ResponseEntity<SuccessResponse> rejectEscalatedPlan(
            @PathVariable Long planId,
            @RequestBody EscalatedPlanDecisionRequest request) {
        try {
            superAdminPlanService.rejectEscalatedPlan(planId, request.reason());
            return ResponseEntity.ok(new SuccessResponse("Plan rejected successfully", null));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(new SuccessResponse("Plan not found", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new SuccessResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/{planId}/preview-pdf")
    public ResponseEntity<byte[]> previewEscalatedPlanPdf(@PathVariable Long planId) {
        byte[] pdf = superAdminPlanService.previewPlanPdf(planId);
        return pdfResponse(pdf, "escalated-plan-" + planId + ".pdf");
    }

    @GetMapping("/{planId}/preview-summary")
    public ResponseEntity<byte[]> previewEscalatedPlanSummary(@PathVariable Long planId) {
        byte[] pdf = superAdminPlanService.previewSummaryPdf(planId);
        return pdfResponse(pdf, "escalated-plan-" + planId + "-summary.pdf");
    }

    public record EscalatedPlanDecisionRequest(String reason) {}

    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String filename) {
        ContentDisposition disposition = ContentDisposition.inline()
                .filename(filename, StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private EscalatedPlanResponse toEscalatedPlanResponse(EscalatedPlanProjection plan) {
        List<AssignedDoctorDto> assignedDoctors = assignmentRepository.findByTravelPlanIdAndDeletedAtIsNull(plan.getId()).stream()
                .map(a -> {
                    User d = a.getDoctor();
                    return new AssignedDoctorDto(
                            d.getId(),
                            d.getFirstName(),
                            d.getLastName(),
                            d.getEmail(),
                            d.getAvatarUrl());
                })
                .toList();
        Boolean openToAllDoctors = assignedDoctors.isEmpty() ? true : null;
        return new EscalatedPlanResponse(
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
                plan.getEscalatedAt(),
                assignedDoctors,
                openToAllDoctors);
    }

    private String fullName(String firstNameValue, String lastNameValue, String fallbackName) {
        String firstName = firstNameValue != null ? firstNameValue : "";
        String lastName = lastNameValue != null ? lastNameValue : "";
        String name = (firstName + " " + lastName).trim();
        return !name.isBlank() ? name : (fallbackName != null ? fallbackName : "");
    }
}
