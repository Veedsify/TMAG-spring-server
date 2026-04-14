package com.TravelMedicineAdvisory.Server.domain.user;

import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.domain.role.Role;
import com.TravelMedicineAdvisory.Server.domain.role.RoleRepository;
import com.TravelMedicineAdvisory.Server.domain.useronboarding.QuestionnaireProgressService;
import com.TravelMedicineAdvisory.Server.domain.useronboarding.UserOnboardingRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final UserOnboardingRepository onboardingRepository;
    private final QuestionnaireProgressService progressService;

    public UserService(UserRepository repository, RoleRepository roleRepository,
            UserOnboardingRepository onboardingRepository,
            QuestionnaireProgressService progressService) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.onboardingRepository = onboardingRepository;
        this.progressService = progressService;
    }

    public Page<UserResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public UserResponse findById(Long id) {
        User entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return toResponse(entity);
    }

    public UserResponse create(UserRequest request) {
        User entity = new User();
        mapRequestToEntity(request, entity);
        User saved = repository.save(entity);
        return toResponse(saved);
    }

    public UserResponse update(Long id, UserRequest request) {
        User entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        mapRequestToEntity(request, entity);
        User saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        // Clean up onboarding data: Redis progress + DB onboarding record
        progressService.delete(user.getEmail());
        onboardingRepository.findByUser_Email(user.getEmail())
                .ifPresent(onboardingRepository::delete);

        repository.deleteById(id);
    }

    private UserResponse toResponse(User entity) {
        return new UserResponse(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getName(),
                entity.getUsername(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getOnboardingStage(),
                entity.getOnboarded(),
                entity.getVerified(),
                entity.getLastLogin(),
                entity.getAvatarUrl(),
                entity.getCredits(),
                entity.getType(),
                entity.getRole() != null ? entity.getRole().getId() : null,
                entity.getBillingCurrency(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCreditPlan() != null
                        ? com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanResponse.from(entity.getCreditPlan())
                        : null);
    }

    private void mapRequestToEntity(UserRequest request, User entity) {
        entity.setFirstName(request.firstName());
        entity.setLastName(request.lastName());
        entity.setName(request.name());
        entity.setUsername(request.username());
        entity.setPhone(request.phone());
        entity.setEmail(request.email());
        entity.setOnboardingStage(request.onboardingStage());
        entity.setOnboarded(request.onboarded());
        entity.setVerified(request.isVerified());
        entity.setAvatarUrl(request.avatarUrl());
        entity.setCredits(request.credits());
        entity.setType(request.type());
        if (request.billingCurrency() != null) {
            entity.setBillingCurrency(request.billingCurrency());
        }
        if (request.roleId() != null) {
            Role role = roleRepository.findById(request.roleId())
                    .orElseThrow(() -> new NoSuchElementException("Role not found"));
            entity.setRole(role);
        }
    }
}
