package com.TravelMedicineAdvisory.Server.domain.companyadmin.management;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.TravelMedicineAdvisory.Server.security.AppUserDetails;
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

import jakarta.validation.Valid;

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
    @PreAuthorize("@perm.company(authentication, #companyId, 'company_user:list', 'employee:list')")
    public ResponseEntity<SuccessResponse> viewTeamMembers(@RequestParam Long companyId) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.viewTeamMembers(companyId)));
    }

    @GetMapping("/users")
    @PreAuthorize("@perm.company(authentication, #companyId, 'company_user:list', 'employee:list')")
    public ResponseEntity<SuccessResponse> listUsers(@RequestParam Long companyId) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.viewTeamMembers(companyId)));
    }

    @PostMapping("/users")
    @PreAuthorize("@perm.company(authentication, #request.companyId(), 'company_user:create', 'employee:create')")
    public ResponseEntity<SuccessResponse> createUser(@RequestBody CompanyAdminUserCreateRequest request) {
        return ResponseEntity.ok(new SuccessResponse("Created successfully", service.createUser(request)));
    }

    @PutMapping("/users/{companyUserId}")
    @PreAuthorize("@perm.has(authentication, 'company_user:update', 'employee:update')")
    public ResponseEntity<SuccessResponse> updateUser(@PathVariable Long companyUserId,
            @RequestBody CompanyAdminUserUpdateRequest request) {
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", service.updateCompanyUser(companyUserId, request)));
    }

    @DeleteMapping("/users/{companyUserId}")
    @PreAuthorize("@perm.has(authentication, 'company_user:delete')")
    public ResponseEntity<SuccessResponse> deleteUser(@PathVariable Long companyUserId,
            @AuthenticationPrincipal AppUserDetails authUser) {
        service.deleteCompanyUser(companyUserId, authUser.getUserId());
        return ResponseEntity.ok(new SuccessResponse("Deleted successfully", null));
    }

    @PutMapping("/users/{companyUserId}/restrict-access")
    @PreAuthorize("@perm.has(authentication, 'company_user:update', 'employee:update')")
    public ResponseEntity<SuccessResponse> restrictUserAccess(@PathVariable Long companyUserId,
            @RequestBody CompanyAdminAccessRequest request) {
        boolean restricted = request != null && Boolean.TRUE.equals(request.restricted());
        return ResponseEntity.ok(new SuccessResponse("Access updated",
                service.restrictUserAccess(companyUserId, restricted)));
    }

    @DeleteMapping("/employees/{employeeId}")
    @PreAuthorize("@perm.company(authentication, #companyId, 'employee:delete')")
    public ResponseEntity<SuccessResponse> removeEmployee(@PathVariable Long employeeId, @RequestParam Long companyId) {
        service.removeEmployee(employeeId, companyId);
        return ResponseEntity.ok(new SuccessResponse("Deleted successfully", null));
    }

    @GetMapping("/export/company-data")
    @PreAuthorize("@perm.company(authentication, #companyId, 'data_export:read', 'company:read', 'employee:list')")
    public ResponseEntity<SuccessResponse> exportCompanyData(@RequestParam Long companyId) {
        return ResponseEntity.ok(new SuccessResponse("Export generated successfully", service.exportCompanyData(companyId)));
    }

    @PostMapping("/allocate-credits")
    @PreAuthorize("@perm.company(authentication, #request.companyId(), 'credit:update', 'employee:update')")
    public ResponseEntity<SuccessResponse> allocateCreditsToUser(
            @Valid @RequestBody CompanyAdminCreditAllocationRequest request) {
        return ResponseEntity.ok(new SuccessResponse("Credits allocated successfully",
                service.allocateCreditsToUser(request)));
    }

    @DeleteMapping("/companies/{companyId}")
    @PreAuthorize("@perm.admin(authentication, 'company:delete')")
    public ResponseEntity<SuccessResponse> deleteCompany(@PathVariable Long companyId) {
        service.deleteCompany(companyId);
        return ResponseEntity.ok(new SuccessResponse("Company deleted successfully", null));
    }
}
