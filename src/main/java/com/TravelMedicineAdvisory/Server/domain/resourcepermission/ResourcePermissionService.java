package com.TravelMedicineAdvisory.Server.domain.resourcepermission;

import com.TravelMedicineAdvisory.Server.domain.permission.Permission;
import com.TravelMedicineAdvisory.Server.domain.permission.PermissionRepository;
import com.TravelMedicineAdvisory.Server.domain.role.Role;
import com.TravelMedicineAdvisory.Server.domain.role.RoleRepository;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Page<ResourcePermissionResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public ResourcePermissionResponse findById(Long id) {
        ResourcePermission entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("ResourcePermission not found"));
        return toResponse(entity);
    }

    public ResourcePermissionResponse create(ResourcePermissionRequest request) {
        ResourcePermission entity = new ResourcePermission();
        mapRequestToEntity(request, entity);
        ResourcePermission saved = repository.save(entity);
        return toResponse(saved);
    }

    public ResourcePermissionResponse update(Long id, ResourcePermissionRequest request) {
        ResourcePermission entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("ResourcePermission not found"));
        mapRequestToEntity(request, entity);
        ResourcePermission saved = repository.save(entity);
        return toResponse(saved);
    }

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
