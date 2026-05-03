package com.TravelMedicineAdvisory.Server.domain.admin.doctor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorApplicationStatus;
import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorApplication;
import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorApplicationRepository;
import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorInvitationRequest;
import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorValidationService;
import com.TravelMedicineAdvisory.Server.domain.role.Role;
import com.TravelMedicineAdvisory.Server.domain.role.RoleRepository;
import com.TravelMedicineAdvisory.Server.domain.role.Roles;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.AvatarUrlService;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.TravelMedicineAdvisory.Server.domain.usersetting.UserSetting;
import com.TravelMedicineAdvisory.Server.domain.usersetting.UserSettingService;

@RestController
@RequestMapping("/api/v1/admin/doctors")
@PreAuthorize("@perm.has(authentication, 'doctor:read', 'doctor:update', 'all')")
public class SuperAdminDoctorController {

    private final DoctorValidationService doctorValidationService;
    private final UserRepository userRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final RoleRepository doctorRoleRepository;
    private final UserSettingService userSettingService;
    private final DoctorApplicationRepository doctorApplicationRepository;
    private final AvatarUrlService avatarUrlService;

    public SuperAdminDoctorController(DoctorValidationService doctorValidationService,
            UserRepository userRepository,
            TravelPlanRepository travelPlanRepository, RoleRepository doctorRoleRepository,
            UserSettingService userSettingService,
            DoctorApplicationRepository doctorApplicationRepository,
            AvatarUrlService avatarUrlService) {
        this.doctorValidationService = doctorValidationService;
        this.doctorRoleRepository = doctorRoleRepository;
        this.userRepository = userRepository;
        this.travelPlanRepository = travelPlanRepository;
        this.userSettingService = userSettingService;
        this.doctorApplicationRepository = doctorApplicationRepository;
        this.avatarUrlService = avatarUrlService;
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
        List<DoctorApplication> applications = doctorApplicationRepository.findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(DoctorApplicationStatus.PENDING);
        List<AdminDoctorApplicationDto> dtos = applications.stream()
                .map(a -> {
                    return new AdminDoctorApplicationDto(
                            a.getId(),
                            valueOrEmpty(a.getFirstName()),
                            valueOrEmpty(a.getLastName()),
                            valueOrEmpty(a.getEmail()),
                            valueOrEmpty(a.getPhone()),
                            valueOrEmpty(a.getMedicalLicenseNumber()),
                            valueOrEmpty(a.getSpecialty()),
                            valueOrEmpty(a.getCountry()),
                            a.getStatus() != null ? a.getStatus().name() : "NONE",
                            a.getCreatedAt() != null ? a.getCreatedAt().toString() : null,
                            null,
                            a.getSignatureUrl(),
                            a.getProfilePictureUrl(),
                            a.isConfidentialityAgreementAccepted(),
                            a.isConductAgreementAccepted(),
                            a.getBio(),
                            a.getCreatedAt() != null ? a.getCreatedAt().toString() : null);
                })
                .toList();
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", dtos));
    }

    @PostMapping("/applications/{userId}/approve")
    public ResponseEntity<SuccessResponse> approveApplication(@PathVariable Long userId) {
        doctorValidationService.approveDoctorApplication(userId);
        return ResponseEntity.ok(new SuccessResponse("Application approved successfully", null));
    }

    @PostMapping("/{userId}/approve")
    public ResponseEntity<SuccessResponse> approveApplicationAlias(@PathVariable Long userId) {
        return approveApplication(userId);
    }

    @PostMapping("/applications/{userId}/reject")
    public ResponseEntity<SuccessResponse> rejectApplication(
            @PathVariable Long userId,
            @RequestBody java.util.Map<String, String> body) {
        String reason = body.getOrDefault("reason", "");
        doctorValidationService.rejectDoctorApplication(userId, reason);
        return ResponseEntity.ok(new SuccessResponse("Application rejected successfully", null));
    }

    @PostMapping("/{userId}/reject")
    public ResponseEntity<SuccessResponse> rejectApplicationAlias(
            @PathVariable Long userId,
            @RequestBody java.util.Map<String, String> body) {
        return rejectApplication(userId, body);
    }

    @PostMapping("/invite")
    public ResponseEntity<SuccessResponse> inviteDoctor(@RequestBody DoctorInvitationRequest request) {
        doctorValidationService.inviteDoctor(request.email(), request.firstName(), request.lastName());
        return ResponseEntity.ok(new SuccessResponse("Doctor invited successfully", null));
    }

    @GetMapping("/stats")
    public ResponseEntity<SuccessResponse> getStats() {
        long totalDoctors = userRepository.findByRole(this.getDoctorRole().getId()).size();
        long pendingApplications = doctorApplicationRepository.countByStatusAndDeletedAtIsNull(DoctorApplicationStatus.PENDING);
        long totalValidatedPlans = travelPlanRepository.countAllActive();
        AdminDoctorStatsDto stats = new AdminDoctorStatsDto(totalDoctors, pendingApplications, 0, totalValidatedPlans);
        return ResponseEntity.ok(new SuccessResponse("Stats fetched successfully", stats));
    }

    @GetMapping
    public ResponseEntity<SuccessResponse> getDoctors() {
        List<User> doctors = userRepository.findByRole(this.getDoctorRole().getId());
        List<AdminDoctorListItemDto> dtos = doctors.stream()
                .map(u -> {
                    long validatedCount = travelPlanRepository.countApprovedByDoctor(u.getId());
                    UserSetting settings = userSettingService.getOrCreateByUserId(u.getId());
                    return new AdminDoctorListItemDto(
                            u.getId(),
                            valueOrEmpty(u.getFirstName()),
                            valueOrEmpty(u.getLastName()),
                            valueOrEmpty(u.getEmail()),
                            valueOrEmpty(u.getPhone()),
                            valueOrEmpty(settings.getMedicalLicenseNumber()),
                            "",
                            avatarUrlService.toFullUrl(u.getAvatarUrl()),
                            valueOrEmpty(u.getBio()),
                            validatedCount,
                            u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
                })
                .toList();
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", Map.of(
                "doctors", dtos,
                "pagination", Map.of(
                        "total", dtos.size(),
                        "page", 1,
                        "pageSize", dtos.size(),
                        "totalPages", 1))));
    }

    @PostMapping("/{userId}/revoke")
    public ResponseEntity<SuccessResponse> revokeDoctor(@PathVariable Long userId) {
        doctorValidationService.revokeDoctor(userId);
        return ResponseEntity.ok(new SuccessResponse("Doctor privileges revoked successfully", null));
    }

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }
}
