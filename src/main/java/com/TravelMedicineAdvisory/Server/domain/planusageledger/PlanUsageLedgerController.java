package com.TravelMedicineAdvisory.Server.domain.planusageledger;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.TravelMedicineAdvisory.Server.core.types.PaginatedResponse;
import com.TravelMedicineAdvisory.Server.core.types.Pagination;
import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.TravelMedicineAdvisory.Server.security.AppUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Plan usage ledgers")
@RestController
@RequestMapping("/api/v1/plan-usage-ledgers")
public class PlanUsageLedgerController {

    private final PlanUsageLedgerService service;
    private final UserRepository userRepository;

    public PlanUsageLedgerController(PlanUsageLedgerService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("@perm.has(authentication, 'plan_usage_ledger:list')")
    public ResponseEntity<SuccessResponse> getAll(Pageable pageable) {
        Page<PlanUsageLedgerResponse> page = service.findAll(pageable);
        Pagination pagination = new Pagination(
                (int) page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages());
        PaginatedResponse<java.util.List<PlanUsageLedgerResponse>> paginatedResponse = new PaginatedResponse<>(page.getContent(),
                pagination);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", paginatedResponse));
    }

    @GetMapping("/my")
    @PreAuthorize("@perm.has(authentication, 'plan_usage_ledger:read')")
    public ResponseEntity<SuccessResponse> getMine(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Long userId = getUserIdFromUserDetails(userDetails);
        Page<PlanUsageLedgerResponse> page = service.findByUserId(userId, pageable);
        Pagination pagination = new Pagination(
                (int) page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages());
        PaginatedResponse<java.util.List<PlanUsageLedgerResponse>> paginatedResponse = new PaginatedResponse<>(page.getContent(),
                pagination);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", paginatedResponse));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("@perm.has(authentication, 'plan_usage_ledger:read')")
    public ResponseEntity<SuccessResponse> getByEmployeeId(
            @PathVariable Long employeeId,
            Pageable pageable) {
        Page<PlanUsageLedgerResponse> page = service.findByEmployeeId(employeeId, pageable);
        Pagination pagination = new Pagination(
                (int) page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages());
        PaginatedResponse<java.util.List<PlanUsageLedgerResponse>> paginatedResponse = new PaginatedResponse<>(page.getContent(),
                pagination);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", paginatedResponse));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'plan_usage_ledger:read')")
    public ResponseEntity<SuccessResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("@perm.has(authentication, 'plan_usage_ledger:create')")
    public ResponseEntity<SuccessResponse> create(@RequestBody PlanUsageLedgerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Created successfully", service.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'plan_usage_ledger:update')")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @RequestBody PlanUsageLedgerRequest request) {
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'plan_usage_ledger:delete')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new SuccessResponse("Deleted successfully", null));
    }

    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        if (userDetails instanceof AppUserDetails appUserDetails) {
            Long userId = appUserDetails.getUserId();
            if (userId != null) {
                return userId;
            }
        }
        String email = userDetails.getUsername();
        if (email != null) {
            return userRepository.findByEmail(email).map(u -> u.getId()).orElse(null);
        }
        throw new RuntimeException("Unable to extract user ID from authentication");
    }
}
