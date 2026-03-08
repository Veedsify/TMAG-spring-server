package com.TravelMedicineAdvisory.Server.domain.healthprofile;

import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class HealthProfileService {

    private final HealthProfileRepository repository;
    private final UserRepository userRepository;

    public HealthProfileService(HealthProfileRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public Page<HealthProfileResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public HealthProfileResponse findById(Long id) {
        HealthProfile entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("HealthProfile not found"));
        return toResponse(entity);
    }

    public HealthProfileResponse create(HealthProfileRequest request) {
        HealthProfile entity = new HealthProfile();
        mapRequestToEntity(request, entity);
        HealthProfile saved = repository.save(entity);
        return toResponse(saved);
    }

    public HealthProfileResponse update(Long id, HealthProfileRequest request) {
        HealthProfile entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("HealthProfile not found"));
        mapRequestToEntity(request, entity);
        HealthProfile saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("HealthProfile not found");
        }
        repository.deleteById(id);
    }

    private HealthProfileResponse toResponse(HealthProfile entity) {
        return new HealthProfileResponse(
            entity.getId(),
            entity.getConditions(),
            entity.getMedications(),
            entity.getAllergies(),
            entity.getBloodType(),
            entity.getEmergencyContactName(),
            entity.getEmergencyContactPhone(),
            entity.getUser() != null ? entity.getUser().getId() : null,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(HealthProfileRequest request, HealthProfile entity) {
        entity.setConditions(request.conditions());
        entity.setMedications(request.medications());
        entity.setAllergies(request.allergies());
        entity.setBloodType(request.bloodType());
        entity.setEmergencyContactName(request.emergencyContactName());
        entity.setEmergencyContactPhone(request.emergencyContactPhone());
        if (request.userId() != null) {
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
            entity.setUser(user);
        }
    }
}
