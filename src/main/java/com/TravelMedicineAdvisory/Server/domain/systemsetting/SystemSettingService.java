package com.TravelMedicineAdvisory.Server.domain.systemsetting;

import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SystemSettingService {

    private final SystemSettingRepository repository;

    public SystemSettingService(SystemSettingRepository repository) {
        this.repository = repository;
    }

    public Page<SystemSettingResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public SystemSettingResponse findById(Long id) {
        SystemSetting entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("SystemSetting not found"));
        return toResponse(entity);
    }

    public SystemSettingResponse create(SystemSettingRequest request) {
        SystemSetting entity = new SystemSetting();
        mapRequestToEntity(request, entity);
        SystemSetting saved = repository.save(entity);
        return toResponse(saved);
    }

    public SystemSettingResponse update(Long id, SystemSettingRequest request) {
        SystemSetting entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("SystemSetting not found"));
        mapRequestToEntity(request, entity);
        SystemSetting saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("SystemSetting not found");
        }
        repository.deleteById(id);
    }

    private SystemSettingResponse toResponse(SystemSetting entity) {
        return new SystemSettingResponse(
            entity.getId(),
            entity.getKey(),
            entity.getValue(),
            entity.getType(),
            entity.getGroup(),
            entity.getLabel(),
            entity.getDescription(),
            entity.getPublic(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(SystemSettingRequest request, SystemSetting entity) {
        entity.setKey(request.key());
        entity.setValue(request.value());
        entity.setType(request.type());
        entity.setGroup(request.group());
        entity.setLabel(request.label());
        entity.setDescription(request.description());
        entity.setPublic(request.isPublic());
    }
}
