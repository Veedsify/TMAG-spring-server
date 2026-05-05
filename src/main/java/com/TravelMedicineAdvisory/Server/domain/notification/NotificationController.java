package com.TravelMedicineAdvisory.Server.domain.notification;

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

@Tag(name = "Notifications")
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@perm.has(authentication, 'notification:list', 'notification:read')")
    public ResponseEntity<SuccessResponse> getAll(Pageable pageable) {
        Page<NotificationResponse> page = service.findAll(pageable);
        Pagination pagination = new Pagination(
                (int) page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages()
        );
        PaginatedResponse<java.util.List<NotificationResponse>> paginatedResponse = new PaginatedResponse<>(page.getContent(), pagination);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", paginatedResponse));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'notification:read')")
    public ResponseEntity<SuccessResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("@perm.has(authentication, 'notification:create')")
    public ResponseEntity<SuccessResponse> create(@RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Created successfully", service.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'notification:update')")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @RequestBody NotificationRequest request) {
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'notification:delete')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new SuccessResponse("Deleted successfully", null));
    }
}
