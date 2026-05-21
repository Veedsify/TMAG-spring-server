package com.TravelMedicineAdvisory.Server.domain.employee;

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

import java.util.Map;

@Tag(name = "Employees")
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@perm.has(authentication, 'employee:list')")
    public ResponseEntity<SuccessResponse> getAll(@RequestParam(required = false) Long companyId, Pageable pageable) {
        Page<EmployeeResponse> page = service.findAll(companyId, pageable);
        Pagination pagination = new Pagination(
                (int) page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages());
        PaginatedResponse<java.util.List<EmployeeResponse>> paginatedResponse = new PaginatedResponse<>(page.getContent(), pagination);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", paginatedResponse));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'employee:read')")
    public ResponseEntity<SuccessResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findById(id)));
    }

    @PostMapping("/invite")
    @PreAuthorize("@perm.has(authentication, 'employee:create')")
    public ResponseEntity<SuccessResponse> invite(@RequestBody EmployeeInviteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Employee invited successfully", service.invite(request)));
    }

    @PostMapping
    @PreAuthorize("@perm.has(authentication, 'employee:create')")
    public ResponseEntity<SuccessResponse> create(@RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Created successfully", service.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'employee:update')")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", service.update(id, request)));
    }

    @PutMapping("/{id}/credits")
    @PreAuthorize("@perm.has(authentication, 'employee:update')")
    public ResponseEntity<SuccessResponse> allocateCredits(@PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(new SuccessResponse("Credits allocated",
                service.allocateCredits(id, body.get("creditsAllocated"))));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@perm.has(authentication, 'employee:update')")
    public ResponseEntity<SuccessResponse> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(new SuccessResponse("Status updated", service.updateStatus(id, body.get("status"))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'employee:delete')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new SuccessResponse("Deleted successfully", null));
    }

    @PostMapping("/{id}/remind-onboarding")
    @PreAuthorize("@perm.has(authentication, 'employee:update')")
    public ResponseEntity<SuccessResponse> remindOnboarding(@PathVariable Long id) {
        service.sendOnboardingReminder(id);
        return ResponseEntity.ok(new SuccessResponse("Reminder sent successfully", null));
    }
}
