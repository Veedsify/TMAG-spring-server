package com.TravelMedicineAdvisory.Server.domain.systemlog;

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

@Tag(name = "System logs")
@RestController
@RequestMapping("/api/v1/system-logs")
public class SystemLogController {

    private final SystemLogService service;

    public SystemLogController(SystemLogService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@perm.has(authentication, 'system_log:list')")
    public ResponseEntity<SuccessResponse> getAll(Pageable pageable) {
        Page<SystemLogResponse> page = service.findAll(pageable);
        Pagination pagination = new Pagination(
                (int) page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages()
        );
        PaginatedResponse<java.util.List<SystemLogResponse>> paginatedResponse = new PaginatedResponse<>(page.getContent(), pagination);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", paginatedResponse));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'system_log:read')")
    public ResponseEntity<SuccessResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("@perm.has(authentication, 'system_log:create')")
    public ResponseEntity<SuccessResponse> create(@RequestBody SystemLogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Created successfully", service.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'system_log:update')")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @RequestBody SystemLogRequest request) {
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'system_log:delete')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new SuccessResponse("Deleted successfully", null));
    }
}
