package com.TravelMedicineAdvisory.Server.domain.resourcepermission;

import java.util.NoSuchElementException;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.core.cache.CacheNames;
import com.TravelMedicineAdvisory.Server.domain.permission.Permission;
import com.TravelMedicineAdvisory.Server.domain.permission.PermissionRepository;
import com.TravelMedicineAdvisory.Server.domain.role.Role;
import com.TravelMedicineAdvisory.Server.domain.role.RoleRepository;

@Service
@Transactional
public class ResourcePermissionService {

    private final ResourcePermissionRepository repository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public ResourcePermissionService(ResourcePermissionRepository repository, RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Cacheable(cacheNames = CacheNames.RESOURCE_PERMISSIONS)
    @Transactional(readOnly = true)
    public Page<ResourcePermissionResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    @Cacheable(cacheNames = CacheNames.RESOURCE_PERMISSIONS)
    @Transactional(readOnly = true)
    public ResourcePermissionResponse findById(Long id) {
        ResourcePermission entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("ResourcePermission not found"));
        return toResponse(entity);
    }

    @CacheEvict(cacheNames = CacheNames.RESOURCE_PERMISSIONS, allEntries = true)
    public ResourcePermissionResponse create(ResourcePermissionRequest request) {
        ResourcePermission entity = new ResourcePermission();
        mapRequestToEntity(request, entity);
        ResourcePermission saved = repository.save(entity);
        return toResponse(saved);
    }

    @CacheEvict(cacheNames = CacheNames.RESOURCE_PERMISSIONS, allEntries = true)
    public ResourcePermissionResponse update(Long id, ResourcePermissionRequest request) {
        ResourcePermission entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("ResourcePermission not found"));
        mapRequestToEntity(request, entity);
        ResourcePermission saved = repository.save(entity);
        return toResponse(saved);
    }

    @CacheEvict(cacheNames = CacheNames.RESOURCE_PERMISSIONS, allEntries = true)
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("ResourcePermission not found");
        }
        repository.deleteById(id);
    }

    private ResourcePermissionResponse toResponse(ResourcePermission entity) {
        return new ResourcePermissionResponse(
            entity.getId(),
            entity.getResourceType(),
            entity.getResourceId(),
            entity.getUserId(),
            entity.getAction(),
            entity.getDefaultScope(),
            entity.getRole() != null ? entity.getRole().getId() : null,
            entity.getPermission() != null ? entity.getPermission().getId() : null,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(ResourcePermissionRequest request, ResourcePermission entity) {
        entity.setResourceType(request.resourceType());
        entity.setResourceId(request.resourceId());
        entity.setUserId(request.userId());
        entity.setAction(request.action());
        entity.setDefaultScope(request.defaultScope());
        if (request.roleId() != null) {
            Role role = roleRepository.findById(request.roleId())
                    .orElseThrow(() -> new NoSuchElementException("Role not found"));
            entity.setRole(role);
        }
        if (request.permissionId() != null) {
            Permission permission = permissionRepository.findById(request.permissionId())
                    .orElseThrow(() -> new NoSuchElementException("Permission not found"));
            entity.setPermission(permission);
        }
    }
}
