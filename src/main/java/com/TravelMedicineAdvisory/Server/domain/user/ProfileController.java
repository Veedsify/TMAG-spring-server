package com.TravelMedicineAdvisory.Server.domain.user;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserService;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanResponse;
import com.TravelMedicineAdvisory.Server.security.AppUserDetails;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Tag(name = "Profile")
@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanyUserService companyUserService;

    public ProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder,
            CompanyUserService companyUserService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.companyUserService = companyUserService;
    }

    @GetMapping("/companies")
    public ResponseEntity<?> getMyCompanies() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Map<String, Object>> companies = companyUserService.findMyCompanies(email);
        return ResponseEntity.ok(Map.of("success", true, "data", companies));
    }

    @GetMapping
    public ResponseEntity<?> getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return ResponseEntity.ok(Map.of("success", true, "data", toResponse(user)));
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal AppUserDetails authUser) {
        User user = userRepository.findByEmail(authUser.getEmail())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        if (request.firstName() != null)
            user.setFirstName(request.firstName());
        if (request.lastName() != null)
            user.setLastName(request.lastName());
        if (request.username() != null)
            user.setUsername(request.username());
        if (request.phone() != null)
            user.setPhone(request.phone());
        if (request.billingCurrency() != null)
            user.setBillingCurrency(request.billingCurrency());
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("success", true, "data", toResponse(user)));
    }

    @PutMapping("/avatar")
    public ResponseEntity<?> updateAvatar(@RequestParam("avatar") MultipartFile file) throws IOException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        String extension = "";
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID() + extension;
        Path uploadDir = Paths.get("storage/avatars");
        Files.createDirectories(uploadDir);
        Files.copy(file.getInputStream(), uploadDir.resolve(filename));

        user.setAvatarUrl("/storage/avatars/" + filename);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("success", true, "data", toResponse(user)));
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Current password is incorrect"));
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("success", true, "message", "Password updated successfully"));
    }

    private ProfileResponse toResponse(User user) {

        Map<String, Object> extendedResponse = Map.of(
                "role_id", user.getRole().getId(),
                "role_name", user.getRole().getName());

        return new ProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getPhone(),
                user.getName(),
                user.getAvatarUrl(),
                user.getType(),
                user.getOnboarded(),
                extendedResponse,
                user.getOnboardingStage(),
                user.getCredits(),
                user.getVerified(),
                user.getRole() != null ? user.getRole().getId() : null,
                user.getBillingCurrency(),
                user.getCreditPlan() != null ? CreditPlanResponse.from(user.getCreditPlan()) : null);
    }
}
