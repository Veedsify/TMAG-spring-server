package com.TravelMedicineAdvisory.Server.domain.admin.companies;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/companies")
@PreAuthorize("hasAuthority('all')")
public class AdminCompanyController {

    private final AdminCompanyService service;

    public AdminCompanyController(AdminCompanyService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<SuccessResponse> getAll() {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<SuccessResponse> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(new SuccessResponse("Created successfully", service.create(body)));
    }

    @GetMapping("/{id}/employees")
    public ResponseEntity<SuccessResponse> getEmployees(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getEmployees(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", service.update(id, updates)));
    }

    @PostMapping("/{id}/freeze")
    public ResponseEntity<SuccessResponse> freeze(@PathVariable Long id) {
        service.freeze(id);
        return ResponseEntity.ok(new SuccessResponse("Company frozen", null));
    }

    @PostMapping("/{id}/unfreeze")
    public ResponseEntity<SuccessResponse> unfreeze(@PathVariable Long id) {
        service.unfreeze(id);
        return ResponseEntity.ok(new SuccessResponse("Company unfrozen", null));
    }

    @PostMapping("/{id}/add-credits")
    public ResponseEntity<SuccessResponse> addCredits(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        service.addCredits(id, body.get("amount"));
        return ResponseEntity.ok(new SuccessResponse("Credits added", null));
    }

    @PostMapping("/{id}/upgrade-tier")
    public ResponseEntity<SuccessResponse> upgradeTier(@PathVariable Long id) {
        service.upgradeTier(id);
        return ResponseEntity.ok(new SuccessResponse("Tier upgraded", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new SuccessResponse("Deleted successfully", null));
    }
}
