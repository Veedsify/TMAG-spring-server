package com.TravelMedicineAdvisory.Server.domain.admin.adminusers;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Admin · Admin users")
@RestController
@RequestMapping("/api/v1/admin/admin-users")
public class AdminAdminUserController {

    private final AdminAdminUserService service;

    public AdminAdminUserController(AdminAdminUserService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@perm.admin(authentication, 'user:list', 'authorization:list')")
    public ResponseEntity<SuccessResponse> getAll() {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.admin(authentication, 'user:read', 'authorization:read')")
    public ResponseEntity<SuccessResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("@perm.admin(authentication, 'user:create', 'authorization:create')")
    public ResponseEntity<SuccessResponse> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Created successfully", service.create(body)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.admin(authentication, 'user:update', 'authorization:update')")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", service.update(id, updates)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.admin(authentication, 'user:delete', 'authorization:delete')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new SuccessResponse("Deleted successfully", null));
    }
}
