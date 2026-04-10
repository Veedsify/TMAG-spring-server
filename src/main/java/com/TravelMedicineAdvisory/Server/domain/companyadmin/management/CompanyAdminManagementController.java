package com.TravelMedicineAdvisory.Server.domain.companyadmin.management;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Company admin · Management")
@RestController
@RequestMapping("/api/v1/company-admin")
public class CompanyAdminManagementController {

    private final CompanyAdminManagementService service;

    public CompanyAdminManagementController(CompanyAdminManagementService service) {
        this.service = service;
    }

    @GetMapping("/team-members")
    public ResponseEntity<SuccessResponse> viewTeamMembers(@RequestParam Long companyId) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.viewTeamMembers(companyId)));
    }

    @GetMapping("/users")
    public ResponseEntity<SuccessResponse> listUsers(@RequestParam Long companyId) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.viewTeamMembers(companyId)));
    }

    @PostMapping("/users")
    public ResponseEntity<SuccessResponse> createUser(@RequestBody CompanyAdminUserCreateRequest request) {
        return ResponseEntity.ok(new SuccessResponse("Created successfully", service.createUser(request)));
    }

    @PutMapping("/users/{companyUserId}")
    public ResponseEntity<SuccessResponse> updateUser(@PathVariable Long companyUserId,
            @RequestBody CompanyAdminUserUpdateRequest request) {
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", service.updateCompanyUser(companyUserId, request)));
    }

    @DeleteMapping("/users/{companyUserId}")
    public ResponseEntity<SuccessResponse> deleteUser(@PathVariable Long companyUserId) {
        service.deleteCompanyUser(companyUserId);
        return ResponseEntity.ok(new SuccessResponse("Deleted successfully", null));
    }

    @PutMapping("/users/{companyUserId}/restrict-access")
    public ResponseEntity<SuccessResponse> restrictUserAccess(@PathVariable Long companyUserId,
            @RequestBody CompanyAdminAccessRequest request) {
        boolean restricted = request != null && Boolean.TRUE.equals(request.restricted());
        return ResponseEntity.ok(new SuccessResponse("Access updated",
                service.restrictUserAccess(companyUserId, restricted)));
    }

    @DeleteMapping("/employees/{employeeId}")
    public ResponseEntity<SuccessResponse> removeEmployee(@PathVariable Long employeeId, @RequestParam Long companyId) {
        service.removeEmployee(employeeId, companyId);
        return ResponseEntity.ok(new SuccessResponse("Deleted successfully", null));
    }

    @GetMapping("/export/company-data")
    public ResponseEntity<SuccessResponse> exportCompanyData(@RequestParam Long companyId) {
        return ResponseEntity.ok(new SuccessResponse("Export generated successfully", service.exportCompanyData(companyId)));
    }

    @DeleteMapping("/companies/{companyId}")
    public ResponseEntity<SuccessResponse> deleteCompany(@PathVariable Long companyId) {
        service.deleteCompany(companyId);
        return ResponseEntity.ok(new SuccessResponse("Company deleted successfully", null));
    }
}
