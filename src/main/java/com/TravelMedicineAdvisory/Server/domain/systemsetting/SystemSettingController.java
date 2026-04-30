package com.TravelMedicineAdvisory.Server.domain.systemsetting;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.TravelMedicineAdvisory.Server.core.types.PaginatedResponse;
import com.TravelMedicineAdvisory.Server.core.types.Pagination;
import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "System settings")
@RestController
@RequestMapping("/api/v1/system-settings")
public class SystemSettingController {

    private final SystemSettingService service;

    public SystemSettingController(SystemSettingService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<SuccessResponse> getAll(Pageable pageable) {
        Page<SystemSettingResponse> page = service.findAll(pageable);
        Pagination pagination = new Pagination(
                (int) page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages()
        );
        PaginatedResponse<java.util.List<SystemSettingResponse>> paginatedResponse = new PaginatedResponse<>(page.getContent(), pagination);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", paginatedResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("@perm.has(authentication, 'system_setting:create')")
    public ResponseEntity<SuccessResponse> create(@RequestBody SystemSettingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Created successfully", service.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'system_setting:update')")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @RequestBody SystemSettingRequest request) {
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'system_setting:delete')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new SuccessResponse("Deleted successfully", null));
    }
}
