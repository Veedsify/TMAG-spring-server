package com.TravelMedicineAdvisory.Server.domain.rolepermission;

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
public class RolePermissionService {

    private final RolePermissionRepository repository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RolePermissionService(RolePermissionRepository repository, RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public Page<RolePermissionResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public RolePermissionResponse findById(Long id) {
        RolePermission entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("RolePermission not found"));
        return toResponse(entity);
    }

    public RolePermissionResponse create(RolePermissionRequest request) {
        RolePermission entity = new RolePermission();
        mapRequestToEntity(request, entity);
        RolePermission saved = repository.save(entity);
        return toResponse(saved);
    }

    public RolePermissionResponse update(Long id, RolePermissionRequest request) {
        RolePermission entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("RolePermission not found"));
        mapRequestToEntity(request, entity);
        RolePermission saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("RolePermission not found");
        }
        repository.deleteById(id);
    }

    private RolePermissionResponse toResponse(RolePermission entity) {
        return new RolePermissionResponse(
            entity.getId(),
            entity.getRole() != null ? entity.getRole().getId() : null,
            entity.getPermission() != null ? entity.getPermission().getId() : null,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(RolePermissionRequest request, RolePermission entity) {
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
