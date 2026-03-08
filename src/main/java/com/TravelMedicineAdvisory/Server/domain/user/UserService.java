package com.TravelMedicineAdvisory.Server.domain.user;

import com.TravelMedicineAdvisory.Server.domain.role.Role;
import com.TravelMedicineAdvisory.Server.domain.role.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;

@Service
@Transactional
public class UserService {

    private final UserRepository repository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository repository, RoleRepository roleRepository) {
        this.repository = repository;
        this.roleRepository = roleRepository;
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
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("User not found");
        }
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
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
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
        if (request.roleId() != null) {
            Role role = roleRepository.findById(request.roleId())
                    .orElseThrow(() -> new NoSuchElementException("Role not found"));
            entity.setRole(role);
        }
    }
}
