package com.TravelMedicineAdvisory.Server.domain.employee;

import com.TravelMedicineAdvisory.Server.core.types.PaginatedResponse;
import com.TravelMedicineAdvisory.Server.core.types.Pagination;
import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<SuccessResponse> getAll(@RequestParam(required = false) Long companyId, Pageable pageable) {
        Page<EmployeeResponse> page = service.findAll(companyId, pageable);
        Pagination pagination = new Pagination(
                (int) page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages()
        );
        PaginatedResponse<EmployeeResponse> paginatedResponse = new PaginatedResponse(page.getContent(), pagination);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", paginatedResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<SuccessResponse> create(@RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Created successfully", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new SuccessResponse("Deleted successfully", null));
    }
}
