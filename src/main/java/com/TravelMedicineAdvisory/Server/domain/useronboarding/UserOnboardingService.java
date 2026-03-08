package com.TravelMedicineAdvisory.Server.domain.useronboarding;

import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserOnboardingService {

    private final UserOnboardingRepository repository;
    private final UserRepository userRepository;

    public UserOnboardingService(UserOnboardingRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public Page<UserOnboardingResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public UserOnboardingResponse findById(Long id) {
        UserOnboarding entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("UserOnboarding not found"));
        return toResponse(entity);
    }

    public UserOnboardingResponse create(UserOnboardingRequest request) {
        UserOnboarding entity = new UserOnboarding();
        mapRequestToEntity(request, entity);
        UserOnboarding saved = repository.save(entity);
        return toResponse(saved);
    }

    public UserOnboardingResponse update(Long id, UserOnboardingRequest request) {
        UserOnboarding entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("UserOnboarding not found"));
        mapRequestToEntity(request, entity);
        UserOnboarding saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("UserOnboarding not found");
        }
        repository.deleteById(id);
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

    private void mapRequestToEntity(UserOnboardingRequest request, UserOnboarding entity) {
        entity.setUserType(request.userType());
        entity.setNationality(request.nationality());
        entity.setCompanyCode(request.companyCode());
        entity.setCompletedAt(request.completedAt());
        if (request.userId() != null) {
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
            entity.setUser(user);
        }
    }
}
