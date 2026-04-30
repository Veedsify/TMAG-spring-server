package com.TravelMedicineAdvisory.Server.domain.admin.users;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;

@Tag(name = "Admin · Users")
@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AdminUserService service;

    public AdminUserController(AdminUserService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("@perm.admin(authentication, 'user:create')")
    public ResponseEntity<SuccessResponse> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(new SuccessResponse("Created successfully", service.create(body)));
    }

    @GetMapping
    @PreAuthorize("@perm.admin(authentication, 'user:list')")
    public ResponseEntity<SuccessResponse> getAll() {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.admin(authentication, 'user:read')")
    public ResponseEntity<SuccessResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.admin(authentication, 'user:update')")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", service.update(id, updates)));
    }

    @PostMapping("/{id}/suspend")
    @PreAuthorize("@perm.admin(authentication, 'user:update')")
    public ResponseEntity<SuccessResponse> suspend(@PathVariable Long id) {
        service.suspend(id);
        return ResponseEntity.ok(new SuccessResponse("User suspended", null));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("@perm.admin(authentication, 'user:update')")
    public ResponseEntity<SuccessResponse> activate(@PathVariable Long id) {
        service.activate(id);
        return ResponseEntity.ok(new SuccessResponse("User activated", null));
    }

    @PostMapping("/{id}/reset-credits")
    @PreAuthorize("@perm.admin(authentication, 'credit:update')")
    public ResponseEntity<SuccessResponse> resetCredits(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        service.resetCredits(id, body.get("amount"));
        return ResponseEntity.ok(new SuccessResponse("Credits reset", null));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("@perm.admin(authentication, 'user:update')")
    public ResponseEntity<SuccessResponse> resetPassword(@PathVariable Long id) {
        service.resetPassword(id);
        return ResponseEntity.ok(new SuccessResponse("Password reset email sent", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.admin(authentication, 'user:delete')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new SuccessResponse("Deleted successfully", null));
    }
}
