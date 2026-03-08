package com.TravelMedicineAdvisory.Server.domain.role;

import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoleService {

    private final RoleRepository repository;

    public RoleService(RoleRepository repository) {
        this.repository = repository;
    }

    public Page<RoleResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public RoleResponse findById(Long id) {
        Role entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Role not found"));
        return toResponse(entity);
    }

    public RoleResponse create(RoleRequest request) {
        Role entity = new Role();
        mapRequestToEntity(request, entity);
        Role saved = repository.save(entity);
        return toResponse(saved);
    }

    public RoleResponse update(Long id, RoleRequest request) {
        Role entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Role not found"));
        mapRequestToEntity(request, entity);
        Role saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Role not found");
        }
        repository.deleteById(id);
    }

    private RoleResponse toResponse(Role entity) {
        return new RoleResponse(
            entity.getId(),
            entity.getName(),
            entity.getPermissions(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(RoleRequest request, Role entity) {
        entity.setName(request.name());
        entity.setPermissions(request.permissions());
    }
}
