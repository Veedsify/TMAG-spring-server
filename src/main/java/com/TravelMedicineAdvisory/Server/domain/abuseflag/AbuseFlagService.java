package com.TravelMedicineAdvisory.Server.domain.abuseflag;

import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AbuseFlagService {

    private final AbuseFlagRepository repository;
    private final UserRepository userRepository;

    public AbuseFlagService(AbuseFlagRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public Page<AbuseFlagResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public AbuseFlagResponse findById(Long id) {
        AbuseFlag entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("AbuseFlag not found"));
        return toResponse(entity);
    }

    public AbuseFlagResponse create(AbuseFlagRequest request) {
        AbuseFlag entity = new AbuseFlag();
        mapRequestToEntity(request, entity);
        AbuseFlag saved = repository.save(entity);
        return toResponse(saved);
    }

    public AbuseFlagResponse update(Long id, AbuseFlagRequest request) {
        AbuseFlag entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("AbuseFlag not found"));
        mapRequestToEntity(request, entity);
        AbuseFlag saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("AbuseFlag not found");
        }
        repository.deleteById(id);
    }

    private AbuseFlagResponse toResponse(AbuseFlag entity) {
        return new AbuseFlagResponse(
            entity.getId(),
            entity.getType(),
            entity.getDescription(),
            entity.getSeverity(),
            entity.getResolved(),
            entity.getResolvedBy(),
            entity.getResolvedAt(),
            entity.getUser() != null ? entity.getUser().getId() : null,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(AbuseFlagRequest request, AbuseFlag entity) {
        entity.setType(request.type());
        entity.setDescription(request.description());
        entity.setSeverity(request.severity());
        entity.setResolved(request.resolved());
        entity.setResolvedBy(request.resolvedBy());
        entity.setResolvedAt(request.resolvedAt());
        if (request.userId() != null) {
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
            entity.setUser(user);
        }
    }
}
