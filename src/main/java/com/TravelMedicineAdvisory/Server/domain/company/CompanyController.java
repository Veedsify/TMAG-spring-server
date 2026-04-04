package com.TravelMedicineAdvisory.Server.domain.company;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.TravelMedicineAdvisory.Server.core.types.PaginatedResponse;
import com.TravelMedicineAdvisory.Server.core.types.Pagination;
import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Companies")
@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {

    private final CompanyService service;

    public CompanyController(CompanyService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<SuccessResponse> getAll(Pageable pageable) {
        Page<CompanyResponse> page = service.findAll(pageable);
        Pagination pagination = new Pagination(
                (int) page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages()
        );
        PaginatedResponse<CompanyResponse> paginatedResponse = new PaginatedResponse(page.getContent(), pagination);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", paginatedResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<SuccessResponse> create(@RequestBody CompanyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Created successfully", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @RequestBody CompanyRequest request) {
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", service.update(id, request)));
    }

    @PostMapping("/{id}/purchase-credits")
    public ResponseEntity<SuccessResponse> purchaseCredits(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer amount = (Integer) body.get("amount");
        String reference = (String) body.getOrDefault("reference", "Credit purchase");
        return ResponseEntity.ok(new SuccessResponse("Credits purchased", service.purchaseCredits(id, amount, reference)));
    }

    @GetMapping("/validate-code")
    public ResponseEntity<SuccessResponse> validateCode(@RequestParam String code) {
        boolean valid = service.validateCompanyCode(code);
        return ResponseEntity.ok(new SuccessResponse("Validated", Map.of("valid", valid)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new SuccessResponse("Deleted successfully", null));
    }
}
