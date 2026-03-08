package com.TravelMedicineAdvisory.Server.domain.resourceaccess;

import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ResourceAccessService {

    private final ResourceAccessRepository repository;

    public ResourceAccessService(ResourceAccessRepository repository) {
        this.repository = repository;
    }

    public Page<ResourceAccessResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public ResourceAccessResponse findById(Long id) {
        ResourceAccess entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("ResourceAccess not found"));
        return toResponse(entity);
    }

    public ResourceAccessResponse create(ResourceAccessRequest request) {
        ResourceAccess entity = new ResourceAccess();
        mapRequestToEntity(request, entity);
        ResourceAccess saved = repository.save(entity);
        return toResponse(saved);
    }

    public ResourceAccessResponse update(Long id, ResourceAccessRequest request) {
        ResourceAccess entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("ResourceAccess not found"));
        mapRequestToEntity(request, entity);
        ResourceAccess saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("ResourceAccess not found");
        }
        repository.deleteById(id);
    }

    private ResourceAccessResponse toResponse(ResourceAccess entity) {
        return new ResourceAccessResponse(
            entity.getId(),
            entity.getRoleId(),
            entity.getMemberId(),
            entity.getResourceType(),
            entity.getResourceId(),
            entity.getAccessType(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(ResourceAccessRequest request, ResourceAccess entity) {
        entity.setRoleId(request.roleId());
        entity.setMemberId(request.memberId());
        entity.setResourceType(request.resourceType());
        entity.setResourceId(request.resourceId());
        entity.setAccessType(request.accessType());
    }
}
