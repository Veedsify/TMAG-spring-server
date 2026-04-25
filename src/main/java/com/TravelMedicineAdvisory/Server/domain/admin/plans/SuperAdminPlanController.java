package com.TravelMedicineAdvisory.Server.domain.admin.plans;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorValidationStatus;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlan;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/admin/plans")
@PreAuthorize("hasRole('SUPERADMIN')")
public class SuperAdminPlanController {

    private final TravelPlanRepository travelPlanRepository;
    private final SuperAdminPlanService superAdminPlanService;

    public SuperAdminPlanController(
            TravelPlanRepository travelPlanRepository,
            SuperAdminPlanService superAdminPlanService) {
        this.travelPlanRepository = travelPlanRepository;
        this.superAdminPlanService = superAdminPlanService;
    }

    /**
     * Get all elevated plans
     */
    @GetMapping("/elevated")
    public ResponseEntity<SuccessResponse> getElevatedPlans(Pageable pageable) {
        Page<TravelPlan> elevatedPlans = travelPlanRepository.findByDoctorValidationStatus(DoctorValidationStatus.ELEVATED, pageable);
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

    public record ElevatedPlanDecisionRequest(String reason) {}
}
