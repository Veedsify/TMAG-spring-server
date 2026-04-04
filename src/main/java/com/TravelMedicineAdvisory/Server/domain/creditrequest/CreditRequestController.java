package com.TravelMedicineAdvisory.Server.domain.creditrequest;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.TravelMedicineAdvisory.Server.core.types.PaginatedResponse;
import com.TravelMedicineAdvisory.Server.core.types.Pagination;
import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.security.AppUserDetails;

@Tag(name = "Credit requests")
@RestController
@RequestMapping("/api/v1/credit-requests")
public class CreditRequestController {

    private final CreditRequestService service;

    public CreditRequestController(CreditRequestService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<SuccessResponse> getAll(@RequestParam(required = false) Long companyId, Pageable pageable) {
        Page<CreditRequestResponse> page = service.findAll(companyId, pageable);
        Pagination pagination = new Pagination(
                (int) page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages()
        );
        PaginatedResponse<CreditRequestResponse> paginatedResponse = new PaginatedResponse(page.getContent(), pagination);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", paginatedResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<SuccessResponse> create(@RequestBody CreditRequestRequest request, @AuthenticationPrincipal AppUserDetails user) {

        Long userId = user.getUserId();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Created successfully", service.create(request, userId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @RequestBody CreditRequestRequest request) {
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", service.update(id, request)));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<SuccessResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Approved", service.approve(id)));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<SuccessResponse> reject(@PathVariable Long id, @RequestBody(required = false) CreditRequestRequest request) {
        String reason = request != null ? request.reason() : null;
        return ResponseEntity.ok(new SuccessResponse("Rejected", service.reject(id, reason)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new SuccessResponse("Deleted successfully", null));
    }
}
