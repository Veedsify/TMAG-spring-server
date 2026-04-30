package com.TravelMedicineAdvisory.Server.domain.doctor;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.core.types.Pagination;
import com.TravelMedicineAdvisory.Server.security.AppUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/doctor")
public class DoctorController {

  private final DoctorValidationService doctorValidationService;

  public DoctorController(DoctorValidationService doctorValidationService) {
    this.doctorValidationService = doctorValidationService;
  }

  @GetMapping("/profile")
  @PreAuthorize("@perm.doctor(authentication, 'profile:read')")
  public ResponseEntity<SuccessResponse> getProfile(@AuthenticationPrincipal AppUserDetails user) {
    return ResponseEntity.ok(new SuccessResponse("Fetched successfully",
        doctorValidationService.getDoctorProfile(user.getUserId())));
  }

  @PostMapping("/onboard")
  @PreAuthorize("@perm.doctor(authentication, 'profile:update')")
  public ResponseEntity<SuccessResponse> onboard(
      @AuthenticationPrincipal AppUserDetails user,
      @RequestParam("medicalLicenseNumber") String medicalLicenseNumber,
      @RequestParam("signature") MultipartFile signature) {
    doctorValidationService.onboardDoctor(user.getUserId(), medicalLicenseNumber, signature);
    return ResponseEntity.ok(new SuccessResponse("Onboarded successfully", null));
  }

  @PostMapping(value = "/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<SuccessResponse> apply(
      @AuthenticationPrincipal AppUserDetails user,
      @RequestParam("medicalLicenseNumber") String medicalLicenseNumber,
      @RequestParam("signature") MultipartFile signature,
      @RequestParam(value = "stamp", required = false) MultipartFile stamp) {
    doctorValidationService.applyToBecomeDoctor(user.getUserId(), medicalLicenseNumber, signature, stamp);
    return ResponseEntity.ok(new SuccessResponse("Application submitted successfully", null));
  }

  @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("@perm.doctor(authentication, 'profile:update')")
  public ResponseEntity<SuccessResponse> updateProfile(
      @AuthenticationPrincipal AppUserDetails user,
      @RequestParam(required = false) String firstName,
      @RequestParam(required = false) String lastName,
      @RequestParam(required = false) String medicalLicenseNumber,
      @RequestParam(value = "signature", required = false) MultipartFile signature,
      @RequestParam(value = "stamp", required = false) MultipartFile stamp) {
    return ResponseEntity.ok(new SuccessResponse("Profile updated successfully",
        doctorValidationService.updateDoctorProfile(
            user.getUserId(), firstName, lastName, medicalLicenseNumber, signature, stamp)));
  }

  // ─── Dashboard ────────────────────────────────────────────

  @GetMapping("/dashboard")
  @PreAuthorize("@perm.doctor(authentication, 'travel_plan:list', 'travel_plan:read')")
  public ResponseEntity<SuccessResponse> getDashboard(@AuthenticationPrincipal AppUserDetails user) {
    return ResponseEntity.ok(new SuccessResponse("Fetched successfully",
        doctorValidationService.getDashboardStats(user.getUserId())));
  }

  // ─── Plan Lists ───────────────────────────────────────────

  @GetMapping("/pending")
  @PreAuthorize("@perm.doctor(authentication, 'travel_plan:list')")
  public ResponseEntity<SuccessResponse> getPendingPlans(Pageable pageable) {
    Page<DoctorValidationPlanDto> page = doctorValidationService.getPendingPlansDto(pageable);
    return ResponseEntity.ok(new SuccessResponse("Fetched successfully",
        Map.of(
            "data", page.getContent(),
            "pagination", new Pagination(
                (int) page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages()))));
  }

  @GetMapping("/validated")
  @PreAuthorize("@perm.doctor(authentication, 'travel_plan:list')")
  public ResponseEntity<SuccessResponse> getValidatedPlans(
      @AuthenticationPrincipal AppUserDetails user,
      Pageable pageable) {
    Page<DoctorValidationPlanDto> page = doctorValidationService.getValidatedPlansDto(user.getUserId(), pageable);
    return ResponseEntity.ok(new SuccessResponse("Fetched successfully",
        Map.of(
            "data", page.getContent(),
            "pagination", new Pagination(
                (int) page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages()))));
  }

  @GetMapping("/plans/pending")
  @PreAuthorize("@perm.doctor(authentication, 'travel_plan:list')")
  public ResponseEntity<SuccessResponse> getPendingPlansLegacy() {
    return ResponseEntity.ok(new SuccessResponse("Fetched successfully",
        doctorValidationService.getPendingPlans()));
  }

  @GetMapping("/plans/approved")
  @PreAuthorize("@perm.doctor(authentication, 'travel_plan:list')")
  public ResponseEntity<SuccessResponse> getApprovedPlans(@AuthenticationPrincipal AppUserDetails user) {
    return ResponseEntity.ok(new SuccessResponse("Fetched successfully",
        doctorValidationService.getApprovedPlans(user.getUserId())));
  }

  @GetMapping("/plans/rejected")
  @PreAuthorize("@perm.doctor(authentication, 'travel_plan:list')")
  public ResponseEntity<SuccessResponse> getRejectedPlans(@AuthenticationPrincipal AppUserDetails user) {
    return ResponseEntity.ok(new SuccessResponse("Fetched successfully",
        doctorValidationService.getRejectedPlans(user.getUserId())));
  }

  // ─── Plan Detail ──────────────────────────────────────────

  @GetMapping("/plans/{id}")
  @PreAuthorize("@perm.doctor(authentication, 'travel_plan:read')")
  public ResponseEntity<SuccessResponse> getPlanDetail(@PathVariable Long id) {
    return ResponseEntity.ok(new SuccessResponse("Fetched successfully",
        doctorValidationService.getPlanDetailDto(id)));
  }

  // ─── Validate (unified approve or reject) ─────────────────

  @PostMapping("/validate")
  @PreAuthorize("@perm.doctor(authentication, 'travel_plan:update')")
  public ResponseEntity<SuccessResponse> validatePlan(
      @AuthenticationPrincipal AppUserDetails user,
      @RequestBody ValidatePlanRequest request) {
    doctorValidationService.validatePlan(
        request.planId(),
        user.getUserId(),
        request.approved(),
        request.rejectionReason());
    String msg = request.approved() ? "Plan approved successfully" : "Plan rejected successfully";
    return ResponseEntity.ok(new SuccessResponse(msg, null));
  }

  @PostMapping("/plans/{id}/approve")
  @PreAuthorize("@perm.doctor(authentication, 'travel_plan:update')")
  public ResponseEntity<SuccessResponse> approvePlan(
      @PathVariable Long id,
      @AuthenticationPrincipal AppUserDetails user) {
    doctorValidationService.approvePlan(id, user.getUserId());
    return ResponseEntity.ok(new SuccessResponse("Plan approved successfully", null));
  }

  @PostMapping("/plans/{id}/reject")
  @PreAuthorize("@perm.doctor(authentication, 'travel_plan:update')")
  public ResponseEntity<SuccessResponse> rejectPlan(
      @PathVariable Long id,
      @AuthenticationPrincipal AppUserDetails user,
      @RequestBody DoctorValidationRequest request) {
    doctorValidationService.rejectPlan(id, user.getUserId(), request.rejectionReason());
    return ResponseEntity.ok(new SuccessResponse("Plan rejected successfully", null));
  }

  // ─── Signed PDF Download ──────────────────────────────────

  @GetMapping("/plans/{id}/signed-pdf")
  @PreAuthorize("@perm.doctor(authentication, 'travel_plan:read')")
  public ResponseEntity<byte[]> downloadSignedPdf(
      @PathVariable Long id,
      @AuthenticationPrincipal AppUserDetails user) {
    byte[] pdf = doctorValidationService.getSignedPdf(id, user.getUserId());
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"signed-travel-plan-" + id + ".pdf\"")
        .contentType(MediaType.APPLICATION_PDF)
        .body(pdf);
  }
}
