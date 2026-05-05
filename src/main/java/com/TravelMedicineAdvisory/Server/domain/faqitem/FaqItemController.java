package com.TravelMedicineAdvisory.Server.domain.faqitem;

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

@Tag(name = "FAQ items")
@RestController
@RequestMapping("/api/v1/faq-items")
public class FaqItemController {

    private final FaqItemService service;

    public FaqItemController(FaqItemService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<SuccessResponse> getAll(Pageable pageable) {
        Page<FaqItemResponse> page = service.findAll(pageable);
        Pagination pagination = new Pagination(
                (int) page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages()
        );
        PaginatedResponse<java.util.List<FaqItemResponse>> paginatedResponse = new PaginatedResponse<>(page.getContent(), pagination);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", paginatedResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("@perm.has(authentication, 'faq_item:create')")
    public ResponseEntity<SuccessResponse> create(@RequestBody FaqItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Created successfully", service.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'faq_item:update')")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @RequestBody FaqItemRequest request) {
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'faq_item:delete')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new SuccessResponse("Deleted successfully", null));
    }
}
