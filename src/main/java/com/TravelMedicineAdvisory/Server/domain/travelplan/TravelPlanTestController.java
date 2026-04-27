package com.TravelMedicineAdvisory.Server.domain.travelplan;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorValidationRequest;
import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorValidationService;
import com.TravelMedicineAdvisory.Server.domain.doctor.ValidatePlanRequest;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;

@RestController
@RequestMapping("/api/v1/test/travel-plans")
public class TravelPlanTestController {

  private final TravelPlanService service;
  private final DoctorValidationService doctorValidationService;
  private final UserRepository userRepository;

  public TravelPlanTestController(
      TravelPlanService service,
      DoctorValidationService doctorValidationService,
      UserRepository userRepository) {
    this.service = service;
    this.doctorValidationService = doctorValidationService;
    this.userRepository = userRepository;
  }

  @PostMapping("/{id}/duplicate")
  public ResponseEntity<SuccessResponse> duplicate(@PathVariable Long id) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new SuccessResponse("Duplicated successfully", service.duplicateForTest(id)));
  }

  @PostMapping("/{id}/approve")
  public ResponseEntity<SuccessResponse> approve(@PathVariable Long id) {
    User doctor = getTestDoctor();
    doctorValidationService.approvePlan(id, doctor.getId());
    return ResponseEntity.ok(new SuccessResponse("Plan approved successfully", null));
  }

  @PostMapping("/{id}/reject")
  public ResponseEntity<SuccessResponse> reject(
      @PathVariable Long id,
      @RequestBody DoctorValidationRequest request) {
    User doctor = getTestDoctor();
    doctorValidationService.rejectPlan(id, doctor.getId(), request.rejectionReason());
    return ResponseEntity.ok(new SuccessResponse("Plan rejected successfully", null));
  }

  @PostMapping("/{id}/validate")
  public ResponseEntity<SuccessResponse> validate(
      @PathVariable Long id,
      @RequestBody ValidatePlanRequest request) {
    User doctor = getTestDoctor();
    doctorValidationService.validatePlan(id, doctor.getId(), request.approved(), request.rejectionReason());
    String msg = request.approved() ? "Plan approved successfully" : "Plan rejected successfully";
    return ResponseEntity.ok(new SuccessResponse(msg, null));
  }

  @GetMapping("/pending")
  public ResponseEntity<SuccessResponse> getPendingPlans() {
    return ResponseEntity.ok(new SuccessResponse("Fetched successfully",
        doctorValidationService.getPendingPlansDto(PageRequest.of(0, 50))));
  }

  @GetMapping("/{id}/review")
  public ResponseEntity<SuccessResponse> getPlanForReview(@PathVariable Long id) {
    return ResponseEntity.ok(new SuccessResponse("Fetched successfully",
        doctorValidationService.getPlanDetailDto(id)));
  }

  private User getTestDoctor() {
    return userRepository.findByRoleName("Doctor")
        .stream()
        .findFirst()
        .orElseThrow(() -> new RuntimeException(
            "No doctor user found for testing. Create a doctor user first."));
  }
}
