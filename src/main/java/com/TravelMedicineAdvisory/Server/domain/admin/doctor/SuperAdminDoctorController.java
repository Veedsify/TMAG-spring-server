package com.TravelMedicineAdvisory.Server.domain.admin.doctor;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorApplicationStatus;
import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorInvitationRequest;
import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorValidationService;
import com.TravelMedicineAdvisory.Server.domain.role.RoleRepository;
import com.TravelMedicineAdvisory.Server.domain.role.Role;
import com.TravelMedicineAdvisory.Server.domain.role.Roles;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/admin/doctors")
@PreAuthorize("hasRole('SUPERADMIN')")
public class SuperAdminDoctorController {

    private final DoctorValidationService doctorValidationService;
    private final UserRepository userRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final RoleRepository doctorRoleRepository;

    public SuperAdminDoctorController(DoctorValidationService doctorValidationService,
            UserRepository userRepository,
            TravelPlanRepository travelPlanRepository, RoleRepository doctorRoleRepository) {
        this.doctorValidationService = doctorValidationService;
        this.doctorRoleRepository = doctorRoleRepository;
        this.userRepository = userRepository;
        this.travelPlanRepository = travelPlanRepository;
    }

    private Role getDoctorRole() {
        Optional<Role> role = doctorRoleRepository.findByName(Roles.Doctor.name());
        if (role.isEmpty()) {
            throw new RuntimeException("Doctor role not found");
        }
        return role.get();
    }

    @GetMapping("/applications")
    public ResponseEntity<SuccessResponse> getApplications() {
        List<User> applications = userRepository.findByDoctorApplicationStatus(DoctorApplicationStatus.PENDING);
        List<Map<String, Object>> dtos = applications.stream()
                .map(u -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("userId", u.getId());
                    dto.put("firstName", u.getFirstName() != null ? u.getFirstName() : "");
                    dto.put("lastName", u.getLastName() != null ? u.getLastName() : "");
                    dto.put("email", u.getEmail() != null ? u.getEmail() : "");
                    dto.put("phone", u.getPhone() != null ? u.getPhone() : "");
                    dto.put("licenseNumber", u.getMedicalLicenseNumber() != null ? u.getMedicalLicenseNumber() : "");
                    dto.put("specialization", "");
                    dto.put("applicationStatus", u.getDoctorApplicationStatus() != null ? u.getDoctorApplicationStatus().name() : "NONE");
                    dto.put("applicationSubmittedAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
                    dto.put("identityDocumentUrl", null);
                    dto.put("licenseDocumentUrl", u.getSignatureUrl());
                    dto.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
                    return dto;
                })
                .toList();
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", dtos));
    }

    @PostMapping("/applications/{userId}/approve")
    public ResponseEntity<SuccessResponse> approveApplication(@PathVariable Long userId) {
        doctorValidationService.approveDoctorApplication(userId);
        return ResponseEntity.ok(new SuccessResponse("Application approved successfully", null));
    }

    @PostMapping("/applications/{userId}/reject")
    public ResponseEntity<SuccessResponse> rejectApplication(
            @PathVariable Long userId,
            @RequestParam String reason) {
        doctorValidationService.rejectDoctorApplication(userId, reason);
        return ResponseEntity.ok(new SuccessResponse("Application rejected successfully", null));
    }

    @PostMapping("/invite")
    public ResponseEntity<SuccessResponse> inviteDoctor(@RequestBody DoctorInvitationRequest request) {
        doctorValidationService.inviteDoctor(request.email(), request.firstName(), request.lastName());
        return ResponseEntity.ok(new SuccessResponse("Doctor invited successfully", null));
    }

    @GetMapping("/stats")
    public ResponseEntity<SuccessResponse> getStats() {
        long totalDoctors = userRepository.findByRole(this.getDoctorRole().getId()).size();
        long pendingApplications = userRepository.findByDoctorApplicationStatus(DoctorApplicationStatus.PENDING).size();
        long totalValidatedPlans = travelPlanRepository.countAllActive();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDoctors", totalDoctors);
        stats.put("pendingApplications", pendingApplications);
        stats.put("approvedToday", 0);
        stats.put("totalValidatedPlans", totalValidatedPlans);
        return ResponseEntity.ok(new SuccessResponse("Stats fetched successfully", stats));
    }

    @GetMapping
    public ResponseEntity<SuccessResponse> getDoctors() {
        List<User> doctors = userRepository.findByRole(this.getDoctorRole().getId());
        List<Map<String, Object>> dtos = doctors.stream()
                .map(u -> {
                    long validatedCount = travelPlanRepository.findApprovedByDoctor(u.getId()).size();
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("userId", u.getId());
                    dto.put("firstName", u.getFirstName() != null ? u.getFirstName() : "");
                    dto.put("lastName", u.getLastName() != null ? u.getLastName() : "");
                    dto.put("email", u.getEmail() != null ? u.getEmail() : "");
                    dto.put("phone", u.getPhone() != null ? u.getPhone() : "");
                    dto.put("licenseNumber", u.getMedicalLicenseNumber() != null ? u.getMedicalLicenseNumber() : "");
                    dto.put("specialization", "");
                    dto.put("validatedPlansCount", validatedCount);
                    dto.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
                    return dto;
                })
                .toList();
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", dtos));
    }

    @PostMapping("/{userId}/revoke")
    public ResponseEntity<SuccessResponse> revokeDoctor(@PathVariable Long userId) {
        doctorValidationService.revokeDoctor(userId);
        return ResponseEntity.ok(new SuccessResponse("Doctor privileges revoked successfully", null));
    }
}
