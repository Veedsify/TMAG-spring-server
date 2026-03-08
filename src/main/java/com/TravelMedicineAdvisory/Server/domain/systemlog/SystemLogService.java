package com.TravelMedicineAdvisory.Server.domain.systemlog;

import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SystemLogService {

    private final SystemLogRepository repository;

    public SystemLogService(SystemLogRepository repository) {
        this.repository = repository;
    }

    public Page<SystemLogResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public SystemLogResponse findById(Long id) {
        SystemLog entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("SystemLog not found"));
        return toResponse(entity);
    }

    public SystemLogResponse create(SystemLogRequest request) {
        SystemLog entity = new SystemLog();
        mapRequestToEntity(request, entity);
        SystemLog saved = repository.save(entity);
        return toResponse(saved);
    }

    public SystemLogResponse update(Long id, SystemLogRequest request) {
        SystemLog entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("SystemLog not found"));
        mapRequestToEntity(request, entity);
        SystemLog saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("SystemLog not found");
        }
        repository.deleteById(id);
    }

    private SystemLogResponse toResponse(SystemLog entity) {
        return new SystemLogResponse(
            entity.getId(),
            entity.getLevel(),
            entity.getMessage(),
            entity.getSource(),
            entity.getDetails(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(SystemLogRequest request, SystemLog entity) {
        entity.setLevel(request.level());
        entity.setMessage(request.message());
        entity.setSource(request.source());
        entity.setDetails(request.details());
    }
}
