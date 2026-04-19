package com.TravelMedicineAdvisory.Server.domain.permission;

import java.util.NoSuchElementException;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.core.cache.CacheNames;

@Service
@Transactional
public class PermissionService {

    private final PermissionRepository repository;

    public PermissionService(PermissionRepository repository) {
        this.repository = repository;
    }

    @Cacheable(cacheNames = CacheNames.PERMISSIONS)
    @Transactional(readOnly = true)
    public Page<PermissionResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    @Cacheable(cacheNames = CacheNames.PERMISSIONS)
    @Transactional(readOnly = true)
    public PermissionResponse findById(Long id) {
        Permission entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Permission not found"));
        return toResponse(entity);
    }

    @CacheEvict(cacheNames = CacheNames.PERMISSIONS, allEntries = true)
    public PermissionResponse create(PermissionRequest request) {
        Permission entity = new Permission();
        mapRequestToEntity(request, entity);
        Permission saved = repository.save(entity);
        return toResponse(saved);
    }

    @CacheEvict(cacheNames = CacheNames.PERMISSIONS, allEntries = true)
    public PermissionResponse update(Long id, PermissionRequest request) {
        Permission entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Permission not found"));
        mapRequestToEntity(request, entity);
        Permission saved = repository.save(entity);
        return toResponse(saved);
    }

    @CacheEvict(cacheNames = CacheNames.PERMISSIONS, allEntries = true)
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Permission not found");
        }
        repository.deleteById(id);
    }

    private PermissionResponse toResponse(Permission entity) {
        return new PermissionResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getResourceType(),
                entity.getAction(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private void mapRequestToEntity(PermissionRequest request, Permission entity) {
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setResourceType(request.resourceType());
        entity.setAction(request.action());
    }
}
