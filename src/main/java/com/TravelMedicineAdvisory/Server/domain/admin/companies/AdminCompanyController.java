package com.TravelMedicineAdvisory.Server.domain.admin.companies;

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

@Tag(name = "Admin · Companies")
@RestController
@RequestMapping("/api/v1/admin/companies")
public class AdminCompanyController {

    private final AdminCompanyService service;

    public AdminCompanyController(AdminCompanyService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@perm.admin(authentication, 'company:list')")
    public ResponseEntity<SuccessResponse> getAll() {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.admin(authentication, 'company:read')")
    public ResponseEntity<SuccessResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("@perm.admin(authentication, 'company:create')")
    public ResponseEntity<SuccessResponse> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(new SuccessResponse("Created successfully", service.create(body)));
    }

    @GetMapping("/{id}/employees")
    @PreAuthorize("@perm.admin(authentication, 'employee:list')")
    public ResponseEntity<SuccessResponse> getEmployees(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getEmployees(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.admin(authentication, 'company:update')")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", service.update(id, updates)));
    }

    @PostMapping("/{id}/freeze")
    @PreAuthorize("@perm.admin(authentication, 'company:update')")
    public ResponseEntity<SuccessResponse> freeze(@PathVariable Long id) {
        service.freeze(id);
        return ResponseEntity.ok(new SuccessResponse("Company frozen", null));
    }

    @PostMapping("/{id}/unfreeze")
    @PreAuthorize("@perm.admin(authentication, 'company:update')")
    public ResponseEntity<SuccessResponse> unfreeze(@PathVariable Long id) {
        service.unfreeze(id);
        return ResponseEntity.ok(new SuccessResponse("Company unfrozen", null));
    }

    @PostMapping("/{id}/add-credits")
    @PreAuthorize("@perm.admin(authentication, 'credit:create', 'credit:update')")
    public ResponseEntity<SuccessResponse> addCredits(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        service.addCredits(id, body.get("amount"));
        return ResponseEntity.ok(new SuccessResponse("Credits added", null));
    }

    @PostMapping("/{id}/upgrade-tier")
    @PreAuthorize("@perm.admin(authentication, 'company:update')")
    public ResponseEntity<SuccessResponse> upgradeTier(@PathVariable Long id) {
        service.upgradeTier(id);
        return ResponseEntity.ok(new SuccessResponse("Tier upgraded", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.admin(authentication, 'company:delete')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new SuccessResponse("Deleted successfully", null));
    }
}
