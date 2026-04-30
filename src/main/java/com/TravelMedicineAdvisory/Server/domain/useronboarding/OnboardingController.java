package com.TravelMedicineAdvisory.Server.domain.useronboarding;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUser;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserRepository;
import com.TravelMedicineAdvisory.Server.domain.employee.Employee;
import com.TravelMedicineAdvisory.Server.domain.employee.EmployeeRepository;
import com.TravelMedicineAdvisory.Server.domain.role.Roles;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.TravelMedicineAdvisory.Server.core.utils.QuestionnaireResponseSanitizer;
import com.fasterxml.jackson.databind.ObjectMapper;

@Tag(name = "Onboarding")
@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {

    private final UserOnboardingRepository onboardingRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CompanyUserRepository companyUserRepository;
    private final EmployeeRepository employeeRepository;
    private final OnboardingQuestionnaireService onboardingQuestionnaireService;
    private final QuestionnaireProgressService progressService;
    private final ObjectMapper objectMapper;

    public OnboardingController(
            UserOnboardingRepository onboardingRepository,
            UserRepository userRepository,
            CompanyRepository companyRepository,
            CompanyUserRepository companyUserRepository,
            EmployeeRepository employeeRepository,
            OnboardingQuestionnaireService onboardingQuestionnaireService,
            QuestionnaireProgressService progressService,
            ObjectMapper objectMapper) {
        this.onboardingRepository = onboardingRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.companyUserRepository = companyUserRepository;
        this.employeeRepository = employeeRepository;
        this.onboardingQuestionnaireService = onboardingQuestionnaireService;
        this.progressService = progressService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    @PreAuthorize("@perm.has(authentication, 'user_onboarding:read')")
    public ResponseEntity<?> getOnboarding() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<UserOnboarding> onboarding = onboardingRepository.findByUser_Email(email);
        Object data = onboarding.map(this::toResponse).orElse(null);
        return ResponseEntity.ok(Map.of("success", true, "data", data != null ? data : Map.of()));
    }

    @PostMapping
    @PreAuthorize("@perm.has(authentication, 'user_onboarding:create', 'user_onboarding:update')")
    public ResponseEntity<?> upsertOnboarding(@RequestBody OnboardingRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        UserOnboarding entity = onboardingRepository.findByUser_Email(email)
                .orElseGet(UserOnboarding::new);

        if (request.userType() != null) {
            entity.setUserType(request.userType());
            user.setType(request.userType());
            userRepository.save(user);
        }
        if (request.nationality() != null)
            entity.setNationality(request.nationality());

        // Validate and link company code when provided
        if (request.companyCode() != null && !request.companyCode().isBlank()) {
            Company company = companyRepository.findByCompanyCode(request.companyCode()).orElse(null);
            if (company == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Invalid company code. Please check and try again."));
            }
            entity.setCompanyCode(request.companyCode());

            String fullName = ((user.getFirstName() != null ? user.getFirstName() : "") +
                    (user.getLastName() != null ? " " + user.getLastName() : "")).trim();
            String resolvedName = fullName.isEmpty() ? user.getEmail() : fullName;

            // Create or update the Employee record (HR/payroll view)
            Employee employee = employeeRepository.findByUser(user).orElseGet(Employee::new);
            employee.setUser(user);
            employee.setCompany(company);
            employee.setName(resolvedName);
            employee.setEmail(user.getEmail());
            if (employee.getStatus() == null) employee.setStatus("active");
            if (employee.getCreditsUsed() == null) employee.setCreditsUsed(0);
            if (employee.getCreditsAllocated() == null) employee.setCreditsAllocated(0);
            if (employee.getPlansGenerated() == null) employee.setPlansGenerated(0);
            employeeRepository.save(employee);

            // Create or update the CompanyUser record (authorization/company membership)
            CompanyUser companyUser = companyUserRepository.findByUser(user).orElseGet(CompanyUser::new);
            companyUser.setUser(user);
            companyUser.setCompany(company);
            companyUser.setRole(String.valueOf(Roles.Individual));
            companyUserRepository.save(companyUser);
        }

        if (Boolean.TRUE.equals(request.complete())) {
            entity.setQuestionnaireCompleted(true);
            entity.setCompletedAt(LocalDateTime.now());
        }

        entity.setUser(user);
        UserOnboarding saved = onboardingRepository.save(entity);
        return ResponseEntity.ok(Map.of("success", true, "data", toResponse(saved)));
    }

    @PutMapping("/stage")
    @PreAuthorize("@perm.has(authentication, 'user_onboarding:update')")
    public ResponseEntity<?> advanceStage(@RequestBody Map<String, Integer> body) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        Integer stage = body.get("stage");
        user.setOnboardingStage(stage);
        if (stage != null && stage >= 5) {
            user.setOnboarded(true);
        }
        userRepository.save(user);
        String roleName = user.getRole() != null ? user.getRole().getName() : "";
        return ResponseEntity.ok(Map.of("success", true, "data", Map.of("stage", stage, "role", roleName)));
    }

    // ─── Questionnaire Questions ─────────────────────────────────

    @GetMapping("/questions")
    @PreAuthorize("@perm.has(authentication, 'user_onboarding:read')")
    public ResponseEntity<?> getQuestions() {
        List<Map<String, Object>> result = onboardingQuestionnaireService.loadQuestionnaireStructure();
        return ResponseEntity.ok(Map.of("success", true, "data", result));
    }

    // ─── Questionnaire Responses ─────────────────────────────────

    @PostMapping("/questionnaire")
    @PreAuthorize("@perm.has(authentication, 'user_onboarding:update')")
    public ResponseEntity<?> saveQuestionnaireResponses(@RequestBody Map<String, Object> body) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        UserOnboarding entity = onboardingRepository.findByUser_Email(email)
                .orElseGet(UserOnboarding::new);

        String responsesJson = "{}";
        try {
            Object responses = body.get("responses");
            responsesJson = responses instanceof String
                    ? (String) responses
                    : objectMapper.writeValueAsString(responses);
        } catch (Exception ignored) {
        }

        boolean complete = Boolean.TRUE.equals(body.get("complete"));

        entity.setUser(user);
        entity.setResponsesJson(QuestionnaireResponseSanitizer.sanitize(responsesJson, objectMapper));

        if (complete) {
            entity.setQuestionnaireCompleted(true);
            entity.setCompletedAt(LocalDateTime.now());
            user.setOnboardingStage(5);
            userRepository.save(user);
            progressService.delete(email);
        }

        UserOnboarding saved = onboardingRepository.save(entity);
        return ResponseEntity.ok(Map.of("success", true, "data", toResponse(saved)));
    }

    // ─── Progress (Redis) ─────────────────────────────────────────

    @PostMapping("/progress")
    @PreAuthorize("@perm.has(authentication, 'user_onboarding:update')")
    public ResponseEntity<?> saveProgress(@RequestBody Map<String, Object> body) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            String progressJson = objectMapper.writeValueAsString(body);
            progressService.save(email, progressJson);
        } catch (Exception ignored) {
        }

        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/progress")
    @PreAuthorize("@perm.has(authentication, 'user_onboarding:read')")
    public ResponseEntity<?> getProgress() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        String progress = progressService.get(email);

        if (progress == null) {
            return ResponseEntity.ok(Map.of("success", true, "data", Map.of()));
        }

        try {
            Object parsed = objectMapper.readValue(progress, Object.class);
            return ResponseEntity.ok(Map.of("success", true, "data", parsed));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", true, "data", Map.of()));
        }
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
                entity.getUpdatedAt(),
                entity.getQuestionnaireCompleted());
    }
}
