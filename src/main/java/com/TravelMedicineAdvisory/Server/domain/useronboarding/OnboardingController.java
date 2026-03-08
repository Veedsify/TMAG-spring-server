package com.TravelMedicineAdvisory.Server.domain.useronboarding;

import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {

    private final UserOnboardingRepository onboardingRepository;
    private final UserRepository userRepository;

    public OnboardingController(UserOnboardingRepository onboardingRepository, UserRepository userRepository) {
        this.onboardingRepository = onboardingRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getOnboarding() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<UserOnboarding> onboarding = onboardingRepository.findByUser_Email(email);
        Object data = onboarding.map(this::toResponse).orElse(null);
        return ResponseEntity.ok(Map.of("success", true, "data", data != null ? data : Map.of()));
    }

    @PostMapping
    public ResponseEntity<?> upsertOnboarding(@RequestBody OnboardingRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        UserOnboarding entity = onboardingRepository.findByUser_Email(email)
                .orElseGet(UserOnboarding::new);

        if (request.userType() != null) entity.setUserType(request.userType());
        if (request.nationality() != null) entity.setNationality(request.nationality());
        if (request.companyCode() != null) entity.setCompanyCode(request.companyCode());
        entity.setUser(user);

        UserOnboarding saved = onboardingRepository.save(entity);
        return ResponseEntity.ok(Map.of("success", true, "data", toResponse(saved)));
    }

    @PutMapping("/stage")
    public ResponseEntity<?> advanceStage(@RequestBody Map<String, Integer> body) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        Integer stage = body.get("stage");
        user.setOnboardingStage(stage);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("success", true, "data", Map.of("stage", stage)));
    }

    private UserOnboardingResponse toResponse(UserOnboarding entity) {
        return new UserOnboardingResponse(
            entity.getId(),
            entity.getUserType(),
            entity.getNationality(),
            entity.getCompanyCode(),
            entity.getCompletedAt(),
            entity.getUser() != null ? entity.getUser().getId() : null,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
