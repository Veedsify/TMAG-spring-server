package com.TravelMedicineAdvisory.Server.domain.user;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserService;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanResponse;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlan;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanCode;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.usersetting.UserSettingService;
import com.TravelMedicineAdvisory.Server.domain.usersetting.UserSettingResponse;
import com.TravelMedicineAdvisory.Server.core.storage.StorageService;
import com.TravelMedicineAdvisory.Server.security.AppUserDetails;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Tag(name = "Profile")
@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private static final long MAX_AVATAR_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final int AVATAR_SIZE = 512;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanyUserService companyUserService;
    private final CreditPlanRepository creditPlanRepository;
    private final UserSettingService userSettingService;
    private final StorageService storageService;
    private final AvatarUrlService avatarUrlService;

    public ProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder,
            CompanyUserService companyUserService, CreditPlanRepository creditPlanRepository,
            UserSettingService userSettingService, StorageService storageService, AvatarUrlService avatarUrlService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.companyUserService = companyUserService;
        this.creditPlanRepository = creditPlanRepository;
        this.userSettingService = userSettingService;
        this.storageService = storageService;
        this.avatarUrlService = avatarUrlService;
    }

    @GetMapping("/companies")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyCompanies() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Map<String, Object>> companies = companyUserService.findMyCompanies(email);
        return ResponseEntity.ok(Map.of("success", true, "data", companies));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return ResponseEntity.ok(Map.of("success", true, "data", toResponse(user)));
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
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
        if (request.avatarUrl() != null)
            user.setAvatarUrl(request.avatarUrl());
        if (request.profilePictureOption() != null)
            user.setProfilePictureOption(request.profilePictureOption());
        if (request.billingCurrency() != null)
            user.setBillingCurrency(request.billingCurrency());
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("success", true, "data", toResponse(user)));
    }

    @PutMapping("/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateAvatar(@RequestParam("avatar") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Profile picture is required");
        }
        if (file.getSize() > MAX_AVATAR_SIZE_BYTES) {
            throw new IllegalArgumentException("Profile picture must be 5MB or smaller");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Profile picture must be an image");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        BufferedImage source = ImageIO.read(file.getInputStream());
        if (source == null) {
            throw new IllegalArgumentException("Profile picture could not be read");
        }

        BufferedImage avatar = cropSquareAndResize(source);
        String filename = UUID.randomUUID() + ".jpg";
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(avatar, "jpg", output)) {
            throw new IOException("JPEG image writer is not available");
        }
        String path = storageService.storeBytes(output.toByteArray(), "avatars", filename, "image/jpeg");

        user.setAvatarUrl(storageService.getUrl(path));
        user.setProfilePictureOption("upload");
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("success", true, "data", toResponse(user)));
    }

    private BufferedImage cropSquareAndResize(BufferedImage source) {
        int side = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - side) / 2;
        int y = (source.getHeight() - side) / 2;
        BufferedImage cropped = source.getSubimage(x, y, side, side);
        BufferedImage output = new BufferedImage(AVATAR_SIZE, AVATAR_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = output.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(cropped, 0, 0, AVATAR_SIZE, AVATAR_SIZE, null);
        graphics.dispose();
        return output;
    }

    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
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

    @PutMapping("/upgrade-plan")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> upgradePlan(@RequestBody UpgradePlanRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        try {
            CreditPlanCode planCode = CreditPlanCode.valueOf(request.planCode().toUpperCase());

            // Only allow STANDARD and PREMIUM for individual users
            if (!planCode.equals(CreditPlanCode.STANDARD) && !planCode.equals(CreditPlanCode.PREMIUM)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Only STANDARD and PREMIUM plans are available for individual users"));
            }

            CreditPlan newPlan = creditPlanRepository.findByCode(planCode)
                    .orElseThrow(() -> new NoSuchElementException("Plan not found: " + planCode));

            // Prevent downgrade
            CreditPlan currentPlan = user.getCreditPlan();
            if (currentPlan != null && isPlanDowngrade(currentPlan.getCode(), planCode)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Cannot downgrade from " + currentPlan.getCode() + " to " + planCode));
            }

            user.setCreditPlan(newPlan);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("success", true, "data", toResponse(user)));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Invalid plan code: " + request.planCode()));
        }
    }

    private boolean isPlanDowngrade(CreditPlanCode currentPlan, CreditPlanCode newPlan) {
        // PREMIUM > STANDARD > ESSENTIAL
        if (currentPlan.equals(CreditPlanCode.PREMIUM)) {
            return newPlan.equals(CreditPlanCode.STANDARD) || newPlan.equals(CreditPlanCode.ESSENTIAL);
        }
        if (currentPlan.equals(CreditPlanCode.STANDARD)) {
            return newPlan.equals(CreditPlanCode.ESSENTIAL);
        }
        return false;
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
                avatarUrlService.toFullUrl(user.getAvatarUrl()),
                user.getProfilePictureOption(),
                user.getType(),
                user.getOnboarded(),
                extendedResponse,
                user.getOnboardingStage(),
                user.getCredits(),
                user.getVerified(),
                user.getRole() != null ? user.getRole().getId() : null,
                user.getBillingCurrency(),
                user.getCreditPlan() != null ? CreditPlanResponse.from(user.getCreditPlan()) : null,
                UserSettingResponse.from(userSettingService.getOrCreateByUserId(user.getId())));
    }

}
